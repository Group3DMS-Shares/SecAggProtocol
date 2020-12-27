package edu.bjut;

import java.math.BigInteger;

import org.junit.Test;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class HashTest {
    
    @Test
    public void testHomomorphicHash() {
        Pairing pairing = PairingFactory.getPairing("aggVote1.properties");
        BigInteger order = pairing.getG1().getOrder();
        Element g = pairing.getG1().newRandomElement().getImmutable();
        System.out.println(g);
        Element gInverse = g.invert();
        Element r = g.mul(g).mul(gInverse);
        System.out.println("result");
        System.out.println(r);
        // BigInteger a = Utils.randomBig(order);
        // BigInteger b = Utils.randomBig(order);

        // BigInteger x1 = Utils.randomBig(order);
        // BigInteger x2 = Utils.randomBig(order);
    }
}
