package edu.bjut.secaggcommon;


import java.math.BigInteger;
import java.security.SecureRandom;

import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import edu.bjut.common.shamir.SecretShare;
import edu.bjut.common.shamir.SecretShareBigInteger;
import edu.bjut.common.shamir.Shamir;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShamirTest {
    
    @Test
    public void testShamir() {
        final SecureRandom random = new SecureRandom();

        Pairing pairing = PairingFactory.getPairing("aggVote1.properties");

        BigInteger order = pairing.getG1().getOrder();
        Element g = pairing.getG1().newRandomElement().getImmutable();

        final BigInteger secret = Shamir.randomBig(order);
        System.out.println("secret: " + secret);
        final BigInteger prime = order;
        // 2 - at least 2 secret parts are needed to view secret
        // 5 - there are 5 persons that get secret parts
        StopWatch time = new StopWatch();
        time.start();
        final SecretShareBigInteger[] shares = Shamir.split(secret, 2, 100, prime, random);
        time.stop();
        System.out.println(time.getLastTaskTimeMillis());
        time.start();
        final SecretShareBigInteger[] t = Shamir.split(secret, 2, 500, prime, random);
        time.stop();
        System.out.println(time.getLastTaskTimeMillis());
        BigInteger orgResult = Shamir.combine(shares, prime);
        System.out.println("orginal result secret is: " + orgResult.toString());

        // we can use any combination of 2 or more parts of secret
        int length = 5;
        SecretShare[] sharesToViewSecret = new SecretShare[length];
        for (int i = 0; i < length; ++i) {
            sharesToViewSecret[i] = new SecretShare(shares[i].getNumber(), g.duplicate().mul(shares[i].getShare()));
        }
        Element result = Shamir.combine2(sharesToViewSecret, pairing, g, prime);

        
        String org = g.duplicate().mul(secret).toString();
        System.out.println("the encrypted secret is: " + org);
        assertTrue(result.toString().equals(org));
        System.out.println("the recovery of encrypted secret is: " + org);
    }
}
