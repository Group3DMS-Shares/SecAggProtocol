package edu.bjut.aggprotocol.entity;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import edu.bjut.common.shamir.SecretShare;
import edu.bjut.common.shamir.SecretShareBigInteger;
import edu.bjut.common.shamir.Shamir;
import edu.bjut.common.big.BigVec;
import edu.bjut.common.messages.ParamsECC;
import edu.bjut.common.util.Params;
import edu.bjut.common.util.Utils;
import edu.bjut.aggprotocol.messages.RegBack;
import edu.bjut.aggprotocol.messages.RegBack2;
import edu.bjut.aggprotocol.messages.RegBack3;
import edu.bjut.aggprotocol.messages.RegMessage;
import edu.bjut.aggprotocol.messages.RegMessage2;
import edu.bjut.aggprotocol.messages.RegMessage3;
import edu.bjut.aggprotocol.messages.RepKeys;
import edu.bjut.aggprotocol.messages.RepMessage;
import edu.bjut.aggprotocol.messages.ShareBuMsg;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class Participant {

    private final static Logger LOG = LoggerFactory.getLogger(Participant.class);
    private final long id = Utils.randomlong();;
    private Pairing pairing;

    private BigInteger di;
    private BigInteger ki;
    private Element qi;
    private Element ri;
    private Element zi;
    private long k;

    private BigInteger order;
    private Element g;
    private int count = 1;

    List<Element> allQi = new ArrayList<Element>();
    List<SecretShareBigInteger> alKi = new ArrayList<SecretShareBigInteger>();
    List<SecretShareBigInteger> othersBu = new ArrayList<SecretShareBigInteger>();

    private BigInteger bi = null;

    private StopWatch stopWatch;

    public Participant(ParamsECC ps) {
        this.pairing = ps.getPairing();
        this.g = ps.getGeneratorOfG1().getImmutable();
        this.order = pairing.getG1().getOrder();

        this.di = Utils.randomBig(order);
        this.ki = Utils.randomBig(order);

        this.ri = this.g.pow(this.di);
        this.qi = this.g.pow(this.ki);
        this.stopWatch = new StopWatch("user_" + this.id);
    }

    public RegMessage genRegMesssage() {
        this.stopWatch.start("register");
        RegMessage reg = new RegMessage(this.id, this.ri, this.qi);
        this.stopWatch.stop();
        return reg;
    }

    public RegMessage2 getRegBack(RegBack back) {
        this.stopWatch.start("xi");
        allQi = back.getAlKeys();

        zi = back.getRi1().duplicate().mul(this.di);
        Element zi1 = back.getRiP1().duplicate().mul(this.di);

        Element xi = zi1.duplicate().sub(zi);
        RegMessage2 reg = new RegMessage2(xi);
        this.stopWatch.stop();
        return reg;
    }

    public RegMessage3 getRegBack2(RegBack2 back2) {
        this.stopWatch.start("ki");
        Element tem = this.zi.duplicate().mul(BigInteger.valueOf(Params.PARTICIPANT_NUM));
        tem.mul(back2.getTi());
        String orgStr = tem.toString();
        this.k = Utils.hash2Big(orgStr, this.order).longValue();
        SecretShareBigInteger[] keys = genKi(Params.PARTICIPANT_NUM);

        RegMessage3 reg3 = new RegMessage3(this.id, keys);
        this.stopWatch.stop();
        return reg3;
    }

    private SecretShareBigInteger[] genKi(int num) {
        SecureRandom random = new SecureRandom();
        SecretShareBigInteger[] shares = Shamir.split(ki, Params.RECOVER_K, Params.PARTICIPANT_NUM, order, random);
        return shares;
    }

    public void getRegBack3(RegBack3 back3) {
        this.stopWatch.start("all_ki");
        this.alKi = back3.getAlKeys();
        this.stopWatch.stop();
    }

    public long getId() {
        return this.id;
    }

    public RepMessage genRepMessage() {
        this.stopWatch.start("report_data");
        if (this.count++ > 2401) {
            this.count = 1;
        }

        BigVec ci = getEncryptedWeights();

        // Element temEle = Utils.hash2ElementG1(ci.toString() + id + count, this.pairing);
        // Element si = temEle.duplicate().mul(this.di);
        Element si = null;

        Element hr = Utils.hash2ElementG1(Integer.toString(count), this.pairing);
        LOG.debug("ki: " + hr.duplicate().mul(this.ki));
        this.stopWatch.stop();
        return new RepMessage(id, ci, si, count);
    }

    private BigVec getEncryptedWeights() {
        BigVec ci = BigVec.One(Params.G_SIZE);
        BigVec pi = genPi();
        BigVec bu = BigVec.genPRGBigVec(this.bi.toString(), Params.G_SIZE);
        LOG.debug("add pi");
        ci = ci.add(pi);
        BigInteger ni = Utils.hash2Big(Long.toString(this.k + this.count), this.order);
        LOG.debug("add ni: " + ni);
        BigVec niVec = BigVec.genPRGBigVec(ni.toString(), Params.G_SIZE);
        ci = ci.add(niVec).add(bu);
        return ci;
    }

    // generates the keys
    public RepKeys genRepKeys(boolean fails[], int num) {
        this.stopWatch.start("fail_recovery");
        SecretShare[] ci = getShares(fails, num);

        // Element temEle = Utils.hash2ElementG1(ci.toString() + id + count, this.pairing);
        // Element si = temEle.duplicate().mul(this.di);
        Element si = null;
        this.stopWatch.stop();
        return new RepKeys(id, ci, si, count);
    }

    private SecretShare[] getShares(boolean fails[], int num) {
        SecretShare[] ci = new SecretShare[num];
        int index = 0;
        Element hr = Utils.hash2ElementG1(Integer.toString(count), pairing);

        for (int i = 0; i < fails.length; i++) {
            if (fails[i]) {
                Element temEle = hr.duplicate().mul(alKi.get(i).getShare());
                ci[index++] = new SecretShare(alKi.get(i).getNumber(), temEle);
            }
        }
        return ci;
    }

    private BigVec genPi() {
        BigVec pi = BigVec.Zero(Params.G_SIZE);
        int index = allQi.indexOf(this.qi);
        Element hr = Utils.hash2ElementG1(Integer.toString(count), pairing);

        for (int i = 0; i < index; i++) {
            Element tem = this.pairing.pairing(allQi.get(i), hr).getImmutable().mul(this.ki);
            LOG.debug(i + " add: " +tem.toBigInteger());
            pi = pi.add(BigVec.genPRGBigVec(tem.toBigInteger().toString(), Params.G_SIZE));
        }
        for (int i = index + 1; i < allQi.size(); i++) {
            Element tem = this.pairing.pairing(allQi.get(i), hr).getImmutable().mul(this.ki);
            LOG.debug(i + " subtract: " +tem.toBigInteger());
            pi = pi.subtract(BigVec.genPRGBigVec(tem.toBigInteger().toString(), Params.G_SIZE));
        }
        return pi;
    }

    public void getRepMessageFails(RepMessage rep, int failNum) {
        this.stopWatch.start("agg_result");
        BigInteger ni = Utils.hash2Big(Long.toString(this.k + this.count), this.order);
        BigVec niVec = BigVec.genPRGBigVec(ni.toString(), Params.G_SIZE);
        niVec = niVec.multiply(BigInteger.valueOf(this.allQi.size() - failNum));
        LOG.info("data: " + rep.getCi().subtract(niVec));
        this.stopWatch.stop();
    }

    public StopWatch getStopWatch() {
        return this.stopWatch;
    }

    public ShareBuMsg genBuMsg() {
        this.stopWatch.start("gen_bu_share");
        this.bi = Utils.randomBig(this.order);
        LOG.info(this.id + " :" + bi);
        SecretShareBigInteger[] shares = Shamir.split(this.bi, Params.RECOVER_K, Params.PARTICIPANT_NUM, order, new SecureRandom());
        this.stopWatch.stop();
        return new ShareBuMsg(this.id, shares);
    }

    public void collectBuShares(List<SecretShareBigInteger> msg) {
        this.othersBu = msg;
    }

    public SecretShareBigInteger[] sendBuShares(boolean[] fails) {
        this.stopWatch.start("recover_bu");
        SecretShareBigInteger[] shares = new SecretShareBigInteger[fails.length];
        for (int i = 0;  i <  shares.length; ++i) {
            if (!fails[i]) {
                shares[i] = this.othersBu.get(i);
            }
        }
        this.stopWatch.stop();
        return shares;
    }

    public Element signFailList(boolean[] fails) {
        this.stopWatch.start("consistency_sign");
        StringBuilder failsString = new StringBuilder();
        for (var b: fails) {
            failsString.append(b);
        }
        var e = Utils.hash2ElementG1(failsString.toString(), pairing);
        var sign = e.mul(this.ki);
        this.stopWatch.stop();
        return sign;
    }

    public void validate(Map<Integer, Element> signMap, boolean[] fails) {
        this.stopWatch.start("consistency_validate");
        StringBuilder failsString = new StringBuilder();
        for (var b: fails) {
            failsString.append(b);
        }
        for (var s: signMap.entrySet()) {
            var e = Utils.hash2ElementG1(failsString.toString(), this.pairing);
            var left = this.pairing.pairing(this.allQi.get(s.getKey()), e);
            var right = this.pairing.pairing(s.getValue(), this.g);
            assert(left.toString().equals(right.toString()));
        }
        this.stopWatch.stop();
    }

}
