package edu.bjut;

import it.unisa.dia.gas.jpbc.PairingParametersGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

import edu.bjut.common.util.Utils;

public class PairingGeneratorsTest {

    @Test
    public void testGen() {
        int rBits = 160;
        int qBits = 512;

        // JPBC Type A pairing generator...
        PairingParametersGenerator<?> pg = new TypeACurveGenerator(rBits, qBits);

        // Then, generate the parameters by invoking the generate method.
        PairingParameters params = pg.generate();
        Pairing pairing = PairingFactory.getPairing(params);
        assertNotNull(pairing);
    }

    @Test
    public void testGenParamsFromProperties() {
        Pairing pairing = PairingFactory.getPairing("aggVote1.properties");
        assertNotNull(pairing);
    }

    @Test
    public void testECDH() {
        Pairing pairing = PairingFactory.getPairing("aggVote1.properties");
        BigInteger order = pairing.getG1().getOrder();
        Element g = pairing.getG1().newRandomElement();

        // gen skA skB
        BigInteger skA = Utils.randomBig(order);
        BigInteger skB = Utils.randomBig(order);

        // gen pkA pkB
        Element pkA = g.duplicate().mul(skA);
        Element pkB = g.duplicate().mul(skB);

        // shared secret
        Element shareAB = pkA.mul(skB);
        Element shareBA = pkB.mul(skA);

        System.out.println("share B->A" + shareAB.toString());
        System.out.println("share A->B" + shareAB.toString());
        assertTrue(shareAB.isEqual(shareBA));

        BigInteger hashShareAB = Utils.hash2Big(shareAB.toString(), order);
        BigInteger hashShareBA = Utils.hash2Big(shareBA.toString(), order);

        System.out.println("hash B->A" + shareAB.toString());
        System.out.println("hash A->B" + shareBA.toString());

        assertTrue(hashShareAB.equals(hashShareBA));
        

    }
    
}
