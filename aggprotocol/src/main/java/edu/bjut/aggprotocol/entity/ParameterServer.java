package edu.bjut.aggprotocol.entity;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import edu.bjut.common.shamir.SecretShare;
import edu.bjut.common.shamir.SecretShareBigInteger;
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
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class ParameterServer {

    private final static Logger LOG = LoggerFactory.getLogger(ParameterServer.class);

    private long id;
    private Pairing pairing;
    private Element g;
    private BigInteger dj;
    private BigInteger order;

    ArrayList<Long> allId = new ArrayList<Long>();
    ArrayList<Element> allKeys = new ArrayList<Element>();
    ArrayList<Element> allQi = new ArrayList<Element>();
    ArrayList<Element> allXi = new ArrayList<Element>();

    private ArrayList<SecretShareBigInteger[]> allki = new ArrayList<SecretShareBigInteger[]>();
    private ArrayList<RepMessage> alRep = new ArrayList<RepMessage>();
    private ArrayList<RepKeys> alRepKeys = new ArrayList<RepKeys>();

    private boolean fails[];
    private int failNum;
    private StopWatch stopWatch = new StopWatch("server");

    public ParameterServer() throws IOException {
        this.pairing = PairingFactory.getPairing("aggVote1.properties");

        this.id = Utils.randomlong();
        this.order = pairing.getG1().getOrder();
        this.g = this.pairing.getG1().newRandomElement().getImmutable();

        this.dj = Utils.randomBig(order);
    }

    public void settingFails(boolean[] fails, int failNum) {
        this.fails = fails;
        this.failNum = failNum;
    }


    public ParamsECC getParamsECC() {
        ParamsECC ps = new ParamsECC(this.pairing, this.g);
        return ps;
    }

    
    /** 
     * The first round in registration phase
     * 
     * @param reg
     */
    public void getRegMessage(RegMessage reg) {
        this.stopWatch.start("register");
        allId.add(reg.getId());
        allKeys.add(reg.getKey());
        allQi.add(reg.getQi());
        this.stopWatch.stop();
    }

    
    /**
     * The first round in registration phase
     * 
     * @param index
     * @return RegBack
     */
    public RegBack genRegBack(int index) {
        this.stopWatch.start("response");
        int pre = (index - 1 + allKeys.size()) % allKeys.size();
        int next = (index + 1) % allKeys.size();

        RegBack back = new RegBack(allQi, allKeys.get(pre), allKeys.get(next));
        this.stopWatch.stop();
        return back;
    }

    
    /** 
     * The second round in registration phase
     *
     * @param reg2
     */
    public void getRegMessage2(RegMessage2 reg2) {
        this.stopWatch.start("collect_xi");
        allXi.add(reg2.getXi());
        this.stopWatch.stop();
    }

    
    /** 
     * The second round in registration phase
     *
     * @param index
     * @return RegBack2
     */
    public RegBack2 genRegBack2(int index) {
        this.stopWatch.start("Ti");
        int n = allXi.size();
        int c = n - 1;
        Element ti = pairing.getG1().newOneElement();
        for (int i = 0; i < n - 1; i++) {
            int next = (index + i) % allKeys.size();
            Element tem = allXi.get(next).duplicate().mul(BigInteger.valueOf(c--));
            ti = ti.duplicate().mul(tem);
        }

        RegBack2 back = new RegBack2(ti);
        this.stopWatch.stop();
        return back;
    }

    
    /** 
     * The third round in registration phase
     * 
     * @param reg2
     */
    public void getRegMessage3(RegMessage3 reg2) {
        this.stopWatch.start("collect_ki");
        this.allki.add(reg2.getKeys());
        this.stopWatch.stop();
    }

    /** 
     * The third round in registration phase
     * 
     * @param reg2
     */
    public RegBack3 genRegBack3(int i) {
        this.stopWatch.start("all_ki");
        ArrayList<SecretShareBigInteger> alkiBack = new ArrayList<SecretShareBigInteger>();

        Iterator<SecretShareBigInteger[]> itKi = allki.iterator();
        while (itKi.hasNext()) {
            SecretShareBigInteger tem = itKi.next()[i];
            alkiBack.add(tem);
        }
        RegBack3 back = new RegBack3(alkiBack);
        this.stopWatch.stop();
        return back;
    }

    public RepMessage getRepMessage(RepMessage rep) {
        RepMessage res = null;
        alRep.add(rep);
        if (alRep.size() < this.allId.size()) {
            return res;
        }
        this.stopWatch.start("sum_all");
        if (false == checkingIncomeMessage()) {
            LOG.warn("check failed at the server");
            return res;
        }
        res = genRepMessage(sumUpReportingData(), rep.getTi());
        this.stopWatch.stop();
        return res; 
    }

    public RepMessage getRepKeys(RepKeys rep) {
        RepMessage res= null;
        alRepKeys.add(rep);
        if (alRepKeys.size() < Params.RECOVER_K) {
            return res;
        }
        this.stopWatch.start("sum_all");
        if (false == checkingRepKeys()) {
            System.out.println("check failed at the agg side");
            return res;
        }
        res = genRepMessage(sumUpFailsData(), rep.getTi());
        this.stopWatch.stop();
        return res;
    }

    /**
     * A meter report multiple types of data to aggregator at a time
     */
    public RepMessage genRepMessage(BigVec data, int count) {
        Element temEle = Utils.hash2ElementG1(data.toString() + id + count, this.pairing);
        Element si = temEle.duplicate().mul(this.dj);
        return new RepMessage(id, data, si, count);
    }

    private BigVec sumUpReportingData() {
        int gSize = alRep.get(0).getCi().size();

        BigVec ci = BigVec.Zero(gSize);
        Iterator<RepMessage> itRep = alRep.iterator();
        while (itRep.hasNext()) {
            ci = ci.add(itRep.next().getCi());
        }
        clearReportMessage();
        return ci;
    }

    private BigVec sumUpFailsData() {
        int gSize = alRep.get(0).getCi().size();
        BigVec ci = BigVec.Zero(gSize);
        Iterator<RepMessage> itRep = alRep.iterator();
        while (itRep.hasNext()) {
            ci = ci.add(itRep.next().getCi());
        }

        Iterator<RepKeys> itRepKeys = alRepKeys.iterator();
        SecretShare[][] keys = new SecretShare[alRepKeys.size()][this.failNum];

        int index = 0;
        while (itRepKeys.hasNext()) {
            keys[index] = itRepKeys.next().getCi();
            index++;
        }
        SecretShare[] sharesToViewSecret = new SecretShare[Params.RECOVER_K];

        int[] pos = getPoss();

        for (int i = 0; i < this.failNum; i++) {
            for (int j = 0; j < Params.RECOVER_K; j++) {
                BigInteger aBigInteger = keys[j][i].getNumber().multiply(BigInteger.valueOf(11));
                Element aElement = keys[j][i].getShare();
                sharesToViewSecret[j] = new SecretShare(aBigInteger, aElement);
            }
            Element tem = combine2(sharesToViewSecret);
            LOG.debug("failure node: " + pos[i]);
            BigVec pi = genPi(tem, pos[i], gSize);
            ci = ci.add(pi);
        }
        clearReportMessage();
        return ci;
    }

    private int[] getPoss() {
        int[] pos = new int[failNum];
        int index = 0;
        for (int i = 0; i < this.fails.length; i++) {
            if (fails[i]) {
                pos[index++] = i;
            }
        }
        return pos;
    }

    private BigVec genPi(Element tems, int index, int gSize) {
        BigVec pi = BigVec.Zero(gSize);

        for (int i = 0; i < index; i++) {
            Element tem = this.pairing.pairing(allQi.get(i), tems);
            pi = pi.add(BigVec.genPRGBigVec(tem.toBigInteger().toString(), gSize));
        }

        for (int i = index + 1; i < allQi.size(); i++) {
            Element tem = this.pairing.pairing(allQi.get(i), tems);
            pi = pi.subtract(BigVec.genPRGBigVec(tem.toBigInteger().toString(), gSize));
        }
        LOG.debug("pi test : " + pi.toString());
        return pi;
    }

    public Element combine2(final SecretShare[] shares) {
        Element accum = this.pairing.getG1().newOneElement();

        for (int formula = 0; formula < shares.length; formula++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int count = 0; count < shares.length; count++) {
                if (formula == count)
                    continue; // If not the same value

                BigInteger startposition = shares[formula].getNumber();
                BigInteger nextposition = shares[count].getNumber();

                numerator = numerator.multiply((nextposition).negate()).mod(this.order);
                denominator = denominator.multiply(startposition.subtract(nextposition)).mod(this.order);
            }

            Element value = shares[formula].getShare();
            BigInteger tmp = (numerator).multiply(modInverse(denominator, this.order));

            Element temEle = value.duplicate().mul(tmp);
            accum = accum.duplicate().add(temEle);
        }
        LOG.info("sec: " + accum);
        return accum;
    }

    private static BigInteger[] gcdD(BigInteger a, BigInteger b) {
        if (b.compareTo(BigInteger.ZERO) == 0)
            return new BigInteger[] { a, BigInteger.ONE, BigInteger.ZERO };
        else {
            BigInteger n = a.divide(b);
            BigInteger c = a.mod(b);
            BigInteger[] r = gcdD(b, c);
            return new BigInteger[] { r[0], r[2], r[1].subtract(r[2].multiply(n)) };
        }
    }

    private static BigInteger modInverse(BigInteger k, BigInteger prime) {
        k = k.mod(prime);
        BigInteger r = (k.compareTo(BigInteger.ZERO) == -1) ? (gcdD(prime, k.negate())[2]).negate() : gcdD(prime, k)[2];
        return prime.add(r).mod(prime);
    }

    protected BigInteger genPi(Element[][] keys, int index, int pos) {
        BigInteger pi = BigInteger.ZERO;

        for (int i = 0; i < index; i++) {
            if (null == keys[pos][i])
                continue;
            BigInteger tem = (keys[pos][i]).toBigInteger();
            pi = pi.add(tem);
        }
        for (int i = index + 1; i < keys[pos].length; i++) {
            if (null == keys[pos][i])
                continue;
            BigInteger tem = (keys[pos][i]).toBigInteger();
            pi = pi.subtract(tem);
        }
        return pi;
    }

    private boolean checkingRepKeys() {
        ArrayList<BigInteger> alFai = prepareFai();

        Element left = PrepareLeftRepKeys(alFai);
        Element right = PrepareRightRepKeys(alFai);

        if (!left.equals(right)) {
            LOG.warn("verify fail.");
        } else {
            LOG.info("recovering keys");
        }
        return true;
    }

    private Element PrepareLeftRepKeys(ArrayList<BigInteger> alFai) {

        Iterator<RepKeys> itRep = alRepKeys.iterator();
        Iterator<BigInteger> itFai = alFai.iterator();

        if (!itRep.hasNext()) {
            return null;
        }

        Element temResult = pairing.getG1().newZeroElement();
        while (itRep.hasNext()) {
            temResult.add(itRep.next().getSi().duplicate().pow(itFai.next()));
        }

        Element result = pairing.pairing(temResult, this.g);
        return result;
    }

    private Element PrepareRightRepKeys(ArrayList<BigInteger> alFai) {

        Iterator<RepKeys> itRep = alRepKeys.iterator();
        Iterator<BigInteger> itFai = alFai.iterator();

        if (!itRep.hasNext()) {
            return null;
        }

        Element result = pairing.getGT().newOneElement();

        Element temHash;
        Element temRi;
        Element temPairing;

        while (itRep.hasNext()) {

            RepKeys rep = itRep.next();
            temHash = Utils.hash2ElementG1(rep.getCi().toString() + rep.getId() + rep.getTi(), this.pairing);

            temRi = getPublicKeyById(rep.getId());
            temPairing = pairing.pairing(temHash, temRi.duplicate().pow(itFai.next()));
            result.mul(temPairing);
        }
        return result;
    }

    private boolean checkingIncomeMessage() {
        ArrayList<BigInteger> alFai = prepareFai();

        Element left = PrepareLeft(alFai);
        Element right = PrepareRight(alFai);

        if (!left.equals(right)) {
            LOG.debug("left :" + left);
            LOG.debug("right : " + right);
            LOG.warn("verification fails");
        }
        return true;
    }

    private ArrayList<BigInteger> prepareFai() {
        ArrayList<BigInteger> alFai = new ArrayList<BigInteger>();
        for (int i = 0; i < Params.PARTICIPANT_NUM; i++) {
            alFai.add(Utils.randomFai());
        }
        return alFai;
    }

    private Element PrepareLeft(ArrayList<BigInteger> alFai) {

        Iterator<RepMessage> itRep = alRep.iterator();
        Iterator<BigInteger> itFai = alFai.iterator();

        if (!itRep.hasNext()) {
            return null;
        }

        Element temResult = pairing.getG1().newZeroElement();
        while (itRep.hasNext()) {
            temResult.add(itRep.next().getSi().duplicate().pow(itFai.next()));
        }

        Element result = pairing.pairing(temResult, this.g);
        return result;
    }

    private Element PrepareRight(ArrayList<BigInteger> alFai) {

        Iterator<RepMessage> itRep = alRep.iterator();
        Iterator<BigInteger> itFai = alFai.iterator();

        if (!itRep.hasNext()) {
            return null;
        }

        Element result = pairing.getGT().newOneElement();

        Element temHash;
        Element temRi;
        Element temPairing;

        while (itFai.hasNext()) {

            RepMessage rep = itRep.next();
            temHash = Utils.hash2ElementG1(rep.getCi().toString() + rep.getId() + rep.getTi(), this.pairing);

            temRi = getPublicKeyById(rep.getId());
            temPairing = pairing.pairing(temHash, temRi.duplicate().pow(itFai.next()));
            result.mul(temPairing);
        }
        return result;
    }

    private Element getPublicKeyById(long id) {
        int index = allId.indexOf(id);
        return allKeys.get(index);
    }

    private void clearReportMessage() {
        alRep.clear();
        allki.clear();
        alRepKeys.clear();
    }

    public void clear() {
        allId.clear();
        allKeys.clear();
        allXi.clear();
        allki.clear();
        allQi.clear();
    }

    public Element assignMeterKeys(long id2, int i) {
        // TODO Auto-generated method stub
        return null;
    }

    public StopWatch getStopWatch() {
        return this.stopWatch;
    }

}
