package edu.bjut;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.junit.Test;

import edu.bjut.Shamir.SecretShare;
import edu.bjut.Shamir.SecretShareBigInteger;
import edu.bjut.Shamir.Shamir;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class ShamirTest {
    
    @Test
	public void testShamir() {
		final SecureRandom random = new SecureRandom();

		Pairing pairing = PairingFactory.getPairing("aggVote1.properties");

		BigInteger order = pairing.getG1().getOrder();
		Element g = pairing.getG1().newRandomElement().getImmutable();

		final BigInteger secret = Shamir.randomBig(order);
		final BigInteger prime = order;
		// 2 - at least 2 secret parts are needed to view secret
		// 5 - there are 5 persons that get secret parts
		final SecretShareBigInteger[] shares = Shamir.split(secret, 2, 50, prime, random);

		// we can use any combination of 2 or more parts of secret
		int length = 5;
		SecretShare[] sharesToViewSecret = new SecretShare[length];
		for (int i = 0; i < length; ++i) {
			sharesToViewSecret[i] = new SecretShare(shares[i].getNumber().multiply(BigInteger.valueOf(10)), g.duplicate().mul(shares[i].getShare()));
		}

		BigInteger orgResult = Shamir.combine(shares, prime);
		System.out.println("orgResult secret is: " + orgResult.toString());
		
		Element result = Shamir.combine2(sharesToViewSecret, pairing, g, prime);
		String org = g.duplicate().mul(secret).toString();
		System.out.println("org secret is: " + org);
		assertTrue(result.toString().equals(org));
	}
}
