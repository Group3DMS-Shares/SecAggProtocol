package edu.bjut.aggprotocol.entity;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.bjut.common.shamir.SecretShare;
import edu.bjut.common.shamir.SecretShareBigInteger;
import edu.bjut.common.shamir.Shamir;
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
    private long id;
    private Pairing pairing;

    private BigInteger di;
    private BigInteger ki;
    private Element qi;
    private Element ri;
    private Element zi;
    private long k;

    private BigInteger order;
    private Element g;
    private int count;

    ArrayList<Element> allQi = new ArrayList<Element>();
    ArrayList<SecretShareBigInteger> alKi = new ArrayList<SecretShareBigInteger>();

    public Participant(ParamsECC ps) throws IOException {

        this.id = Utils.randomlong();
        this.pairing = ps.getPairing();
        this.g = ps.getGeneratorOfG1();

        this.order = pairing.getG1().getOrder();
        this.di = Utils.randomBig(order);

        this.ki = Utils.randomBig(order);

        this.ri = this.g.duplicate().pow(this.di);
        this.qi = this.g.duplicate().pow(this.ki);
    }

    public RegMessage genRegMesssage() {
        RegMessage reg = new RegMessage(this.id, this.ri, this.qi);
        return reg;
    }

    public RegMessage2 getRegBack(RegBack back) {
        allQi = back.getAlKeys();

        zi = back.getRi1().duplicate().mul(this.di);
        Element zi1 = back.getRiP1().duplicate().mul(this.di);

        Element xi = zi1.duplicate().sub(zi);
        RegMessage2 reg = new RegMessage2(xi);
        return reg;
    }

    public RegMessage3 getRegBack2(RegBack2 back2) {
        Element tem = this.zi.duplicate().mul(BigInteger.valueOf(Params.PARTICIPANT_NUM));
        tem.mul(back2.getTi());
        String orgStr = tem.toString();
        this.k = Utils.hash2Big(orgStr, this.order).longValue();
        SecretShareBigInteger[] keys = genKi(Params.PARTICIPANT_NUM);

        RegMessage3 reg3 = new RegMessage3(this.id, keys);
        return reg3;
    }

    private SecretShareBigInteger[] genKi(int num) {
        SecureRandom random = new SecureRandom();
        SecretShareBigInteger[] shares = Shamir.split(ki, Params.RECOVER_K, Params.PARTICIPANT_NUM, order, random);
        return shares;
    }

    public void getRegBack3(RegBack3 back3) {
        this.alKi = back3.getAlKeys();
    }

    public long getId() {
        return this.id;
    }

    public RepMessage genRepMessage() throws IOException {
        if (count++ > 2401)
            count = 1;

        BigInteger ci = getEncryptedWeights();

        Element temEle = Utils.hash2ElementG1(ci.toString() + id + count, this.pairing);
        Element si = temEle.duplicate().mul(this.di);

        Element hr = Utils.hash2ElementG1(Integer.toString(count), pairing);
        LOG.debug("ki: " + hr.duplicate().mul(this.ki));

        return new RepMessage(id, ci, si, count);
    }

    private BigInteger getEncryptedWeights() {
        BigInteger ci = BigInteger.valueOf(1);
        BigInteger pi = genPi();
        ci = ci.add(pi);
        BigInteger ni = Utils.hash2Big(Long.toString(this.k + this.count), this.order);
        ci = ci.add(ni);
        return ci;
    }

    // generates the keys
    public RepKeys genRepKeys(boolean fails[], int num) throws IOException {
        SecretShare[] ci = getShares(fails, num);

        Element temEle = Utils.hash2ElementG1(ci.toString() + id + count, this.pairing);
        Element si = temEle.duplicate().mul(this.di);
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

    private BigInteger genPi() {
        BigInteger pi = BigInteger.ZERO;
        int index = allQi.indexOf(this.qi);
        Element hr = Utils.hash2ElementG1(Integer.toString(count), pairing);

        for (int i = 0; i < index; i++) {
            Element tem = this.pairing.pairing(allQi.get(i), hr).duplicate().mul(this.ki);
            pi = pi.add(tem.toBigInteger());
        }
        for (int i = index + 1; i < allQi.size(); i++) {
            Element tem = this.pairing.pairing(allQi.get(i), hr).duplicate().mul(this.ki);
            pi = pi.subtract(tem.toBigInteger());
        }
        return pi;
    }

    public void getRepMessage(RepMessage rep) throws IOException {
        BigInteger ni = Utils.hash2Big(Long.toString(this.k + this.count), this.order);
        ni = ni.multiply(BigInteger.valueOf(this.allQi.size()));
        LOG.info("data: " + rep.getCi().subtract(ni));
    }

    public void getRepMessageFails(RepMessage rep, int failNum) throws IOException {
        BigInteger ni = Utils.hash2Big(Long.toString(this.k + this.count), this.order);
        ni = ni.multiply(BigInteger.valueOf(this.allQi.size() - failNum));
        LOG.info("data: " + (rep.getCi().subtract(ni)).mod(this.order));
    }

}
