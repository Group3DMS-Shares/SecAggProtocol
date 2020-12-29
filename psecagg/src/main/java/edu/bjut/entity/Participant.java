package edu.bjut.entity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;

import edu.bjut.Shamir.SecretShareBigInteger;
import edu.bjut.messages.MsgRound0;
import edu.bjut.messages.ParamsECC;
import edu.bjut.util.Utils;
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


    private Map<Long, Element> signPubkeys;

    ArrayList<Element> alQi = new ArrayList<Element>();
    ArrayList<SecretShareBigInteger> alKi = new ArrayList<SecretShareBigInteger>();

    public Participant(ParamsECC ps) {

        this.pairing = ps.getPairing();
        this.g = ps.getGeneratorOfG1().getImmutable();
        this.order = pairing.getG1().getOrder();

        this.duSk = Utils.randomBig(order);
        this.duPk = this.g.duplicate().pow(this.duSk);
    }

    public Map<Long, Element> getSignPubkeys() {
        return signPubkeys;
    }

    public void setSignPubkeys(Map<Long, Element> signPubkeys) {
        this.signPubkeys = signPubkeys;
    }


    public MsgRound0 sendMsgRound0() {
        // generate key pairs 
        // (c^PK_u, c^SK_u), 
        this.cSk_u = Utils.randomBig(order);
        this.cPk_u = g.pow(this.cSk_u);
        // (s^PK_u, s^SK_u), sigma_u
        return null;
    }

}
