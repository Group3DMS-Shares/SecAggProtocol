package edu.bjut.psecagg.entity;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import edu.bjut.common.Shamir.SecretShareBigInteger;
import edu.bjut.psecagg.messages.MsgRound0;
import edu.bjut.psecagg.messages.ParamsECC;
import edu.bjut.psecagg.messages.RepKeys;
import edu.bjut.psecagg.messages.RepMessage;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class ParameterServer {

    // pairing parameters
    private Pairing pairing;
    private Element g;
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

        this.order = pairing.getG1().getOrder();
        this.g = this.pairing.getG1().newRandomElement().getImmutable();
    }

    public ParamsECC getParamsECC() {
        ParamsECC ps = new ParamsECC(this.pairing, this.g);
        return ps;
    }


    public void recvMsgRound0(MsgRound0 msgRound0) {

    }
}
