package edu.bjut.entity;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import edu.bjut.Shamir.SecretShareBigInteger;
import edu.bjut.messages.ParamsECC;
import edu.bjut.messages.RepKeys;
import edu.bjut.messages.RepMessage;
import edu.bjut.util.Utils;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class ParameterServer {

    private long id;
    private Pairing pairing;
    private Element g;
    private BigInteger dj;
    private BigInteger order;

    ArrayList<Long> alId = new ArrayList<Long>();
    ArrayList<Element> alKeys = new ArrayList<Element>();
    ArrayList<Element> alQi = new ArrayList<Element>();
    ArrayList<Element> alXi = new ArrayList<Element>();

    private ArrayList<SecretShareBigInteger[]> alki = new ArrayList<SecretShareBigInteger[]>();
    private ArrayList<RepMessage> alRep = new ArrayList<RepMessage>();
    private ArrayList<RepKeys> alRepKeys = new ArrayList<RepKeys>();

    public ParameterServer() throws IOException {

        super();
        this.pairing = PairingFactory.getPairing("aggVote1.properties");

        this.id = Utils.randomlong();
        this.order = pairing.getG1().getOrder();
        this.g = this.pairing.getG1().newRandomElement().getImmutable();

        this.dj = Utils.randomBig(order);
    }

    public ParamsECC getParamsECC() {
        ParamsECC ps = new ParamsECC(this.pairing, this.g);
        return ps;
    }


}
