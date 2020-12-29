package edu.bjut.psecagg.entity;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;

import edu.bjut.common.Shamir.SecretShareBigInteger;
import edu.bjut.common.Shamir.Shamir;
import edu.bjut.common.messages.ParamsECC;
import edu.bjut.common.util.Params;
import edu.bjut.common.util.Utils;
import edu.bjut.psecagg.messages.MsgResponseRound0;
import edu.bjut.psecagg.messages.MsgRound0;
import edu.bjut.psecagg.messages.MsgRound1;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class Participant {

    // pairing parameters
    private Pairing pairing;
    private BigInteger order;
    private Element g;

    // for signature
    private BigInteger duSk;
    private Element duPk;

    // round 0 keys
    private BigInteger cSk_u;
    private Element cPk_u;

    private BigInteger sSk_u;
    private Element sPk_u;

    private long id;

    private Map<Long, Element> signPubKeys;

    ArrayList<Element> alQi = new ArrayList<Element>();
    ArrayList<SecretShareBigInteger> alKi = new ArrayList<SecretShareBigInteger>();

    public Participant(ParamsECC ps) {

        this.id = Utils.incrementId();
        this.pairing = ps.getPairing();
        this.g = ps.getGeneratorOfG1().getImmutable();
        this.order = pairing.getG1().getOrder();

        this.duSk = Utils.randomBig(order);
        this.duPk = this.g.duplicate().pow(this.duSk);
    }

    public long getId() {
        return id;
    }

    public Map<Long, Element> getSignPubKeys() {
        return signPubKeys;
    }

    public void setSignPubKeys(Map<Long, Element> signPubKeys) {
        this.signPubKeys = signPubKeys;
    }


    public MsgRound0 sendMsgRound0() {
        // generate key pairs 
        // (c^PK_u, c^SK_u), 
        this.cSk_u = Utils.randomBig(order);
        this.cPk_u = g.pow(this.cSk_u).getImmutable();
        // (s^PK_u, s^SK_u), sigma_u
        this.sSk_u = Utils.randomBig(order);
        this.sPk_u = g.pow(this.cSk_u).getImmutable();
        // sigma_u
        Element hash = Utils.hash2ElementG1(this.cPk_u.toString() + this.sPk_u,
                this.pairing);
        Element sigma_u = hash.mul(this.duSk);
        MsgRound0 msgRound0 = new MsgRound0(this.id, this.cPk_u, this.sPk_u, sigma_u);
        return msgRound0;
    }

    public BigInteger getDuSk() {
        return duSk;
    }

    public void setDuSk(BigInteger duSk) {
        this.duSk = duSk;
    }

    public Element getDuPk() {
        return duPk;
    }

    public void setDuPk(Element duPk) {
        this.duPk = duPk;
    }

    public BigInteger getcSk_u() {
        return cSk_u;
    }

    public void setcSk_u(BigInteger cSk_u) {
        this.cSk_u = cSk_u;
    }

    public Element getcPk_u() {
        return cPk_u;
    }

    public void setcPk_u(Element cPk_u) {
        this.cPk_u = cPk_u;
    }

	public MsgRound1 sendMsgRound1(MsgResponseRound0 msgResponse) {
        var msg = msgResponse.getPubKeys();
        // TODO verify signature
        for (var m : msg) {
            if (this.id == m.getId()) continue;
            if (verifySign(m.getcPk_u(), m.getsPk_u(), m.getSigma_u()) == false)
                return null;
        }
        // sample b_u
        BigInteger b_u = Utils.randomBig(order);
        SecureRandom random = new SecureRandom();
        SecretShareBigInteger[] shares = Shamir.split(b_u, Params.RECOVER_K, Params.PARTICIPANT_NUM - 1, order, random);
		return null;
    }
    
    private boolean verifySign(Element getcPk_u, Element getsPk_u, Element sigma_u) {
        return false;
    }

}
