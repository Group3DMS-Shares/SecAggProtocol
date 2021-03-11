package edu.bjut.aggprotocol.entity;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

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

    ArrayList<Element> allQi = new ArrayList<Element>();
    ArrayList<SecretShareBigInteger> alKi = new ArrayList<SecretShareBigInteger>();

    private int gSize = 1;
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

    public Participant(ParamsECC ps, int gSize) throws IOException {
        this.pairing = ps.getPairing();
        this.g = ps.getGeneratorOfG1().getImmutable();
        this.order = pairing.getG1().getOrder();

        this.di = Utils.randomBig(order);
        this.ki = Utils.randomBig(order);

        this.ri = this.g.pow(this.di);
        this.qi = this.g.pow(this.ki);
        this.gSize = gSize;
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
        BigVec ci = BigVec.One(this.gSize);
        BigVec pi = genPi();
        LOG.debug("add pi");
        ci = ci.add(pi);
        BigInteger ni = Utils.hash2Big(Long.toString(this.k + this.count), this.order);
        LOG.debug("add ni: " + ni);
        BigVec niVec = BigVec.genPRGBigVec(ni.toString(), this.gSize);
        ci = ci.add(niVec);
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
        BigVec pi = BigVec.Zero(this.gSize);
        int index = allQi.indexOf(this.qi);
        Element hr = Utils.hash2ElementG1(Integer.toString(count), pairing);

        for (int i = 0; i < index; i++) {
            Element tem = this.pairing.pairing(allQi.get(i), hr).getImmutable().mul(this.ki);
            LOG.debug(i + " add: " +tem.toBigInteger());
            pi = pi.add(BigVec.genPRGBigVec(tem.toBigInteger().toString(), this.gSize));
        }
        for (int i = index + 1; i < allQi.size(); i++) {
            Element tem = this.pairing.pairing(allQi.get(i), hr).getImmutable().mul(this.ki);
            LOG.debug(i + " subtract: " +tem.toBigInteger());
            pi = pi.subtract(BigVec.genPRGBigVec(tem.toBigInteger().toString(), this.gSize));
        }
        return pi;
    }

    public void getRepMessageFails(RepMessage rep, int failNum) {
        this.stopWatch.start("agg_result");
        BigInteger ni = Utils.hash2Big(Long.toString(this.k + this.count), this.order);
        BigVec niVec = BigVec.genPRGBigVec(ni.toString(), this.gSize);
        niVec = niVec.multiply(BigInteger.valueOf(this.allQi.size() - failNum));
        LOG.info("data: " + rep.getCi().subtract(niVec));
        this.stopWatch.stop();
    }

    public StopWatch getStopWatch() {
        return this.stopWatch;
    }

}
