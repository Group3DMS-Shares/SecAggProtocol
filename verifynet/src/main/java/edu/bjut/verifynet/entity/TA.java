package edu.bjut.verifynet.entity;

import java.math.BigInteger;

import edu.bjut.verifynet.message.MessageKeys;
import edu.bjut.common.messages.ParamsECC;
import edu.bjut.common.util.Utils;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class TA {

    private BigInteger q;
    private BigInteger dj;

    private Pairing pairing;
    private Element g;

    private long id;

    public TA() {
        this.pairing = PairingFactory.getPairing("aggVote1.properties");
        this.g = this.pairing.getG1().newRandomElement().getImmutable();
        this.q = this.pairing.getG1().getOrder();
        this.dj = Utils.randomBig(q);
        this.id = Utils.randomlong();
    }

    public MessageKeys genUserKeyPair() {
        BigInteger delta = Utils.randomBig(q);
        BigInteger rho = Utils.randomBig(q);

        BigInteger n_sK_n = Utils.randomBig(q);
        Element n_pK_n = this.g.duplicate().mul(n_sK_n);

        BigInteger p_sK_n = Utils.randomBig(q);
        Element p_pK_n = this.g.duplicate().mul(p_sK_n);

        MessageKeys mesKeys = new MessageKeys(delta, rho, n_sK_n, n_pK_n, p_sK_n, p_pK_n);
        return mesKeys;
    }

    public ParamsECC getParamsECC() {
        return new ParamsECC(this.pairing, this.g);
    }
    
}
