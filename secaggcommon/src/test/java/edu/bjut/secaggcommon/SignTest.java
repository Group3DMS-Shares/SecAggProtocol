package edu.bjut.secaggcommon;

import java.math.BigInteger;


import edu.bjut.common.util.Utils;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class SignTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void signatureTest() {

        Pairing pairing = PairingFactory.getPairing("aggVote1.properties");

        BigInteger order = pairing.getG1().getOrder();
        Element g = pairing.getG1().newRandomElement().getImmutable();

        BigInteger signKey = Utils.randomBig(order);
        String msg = "hello world!";
        Element msgE = Utils.hash2ElementG1(msg, pairing).getImmutable();
        System.out.println(msgE);

        Element signature = msgE.mul(signKey);
        System.out.println(signature);

        Element verifyKey = g.mul(signKey);
        System.out.println(pairing.pairing(signature, g));
        System.out.println(pairing.pairing(verifyKey, msgE));
    }
}
