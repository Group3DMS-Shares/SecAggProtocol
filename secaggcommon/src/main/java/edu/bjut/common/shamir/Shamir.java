package edu.bjut.common.shamir;

import java.math.BigInteger;
import java.util.Random;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public final class Shamir {
    public static SecretShareBigInteger[] split(final BigInteger secret, int needed, int available, BigInteger prime,
            Random random) {
//      System.out.println("Prime Number: " + prime);

        final BigInteger[] coeff = new BigInteger[needed];
        coeff[0] = secret;
        for (int i = 1; i < needed; i++) {
            BigInteger r;
            while (true) {
                r = new BigInteger(prime.bitLength(), random);
                if (r.compareTo(BigInteger.ZERO) > 0 && r.compareTo(prime) < 0) {
                    break;
                }
            }
            coeff[i] = r;
        }

        final SecretShareBigInteger[] shares = new SecretShareBigInteger[available];
        for (int x = 1; x <= available; x++) {
            BigInteger accum = secret;

            for (int exp = 1; exp < needed; exp++) {
                accum = accum.add(coeff[exp].multiply(BigInteger.valueOf(x).pow(exp).mod(prime))).mod(prime);
            }
            shares[x - 1] = new SecretShareBigInteger(BigInteger.valueOf(x), accum);
        }

        return shares;
    }

    public static BigInteger combine(final SecretShareBigInteger[] shares, final BigInteger prime) {
        BigInteger accum = BigInteger.ZERO;

        for (int formula = 0; formula < shares.length; formula++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int count = 0; count < shares.length; count++) {
                if (formula == count)
                    continue; // If not the same value

                BigInteger startposition = shares[formula].getNumber();
                BigInteger nextposition = shares[count].getNumber();

                numerator = numerator.multiply(nextposition.negate()).mod(prime);
                denominator = denominator.multiply((startposition.subtract(nextposition))).mod(prime);
            }
            BigInteger value = shares[formula].getShare();
            BigInteger tmp = value.multiply(numerator).multiply(modInverse(denominator, prime));

            accum = prime.add(accum).add(tmp).mod(prime);
        }
        return accum;
    }

    public static Element combine2(final SecretShare[] shares, Pairing pairing, Element g, final BigInteger prime) {
        Element accum = pairing.getG1().newOneElement();

        for (int formula = 0; formula < shares.length; formula++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int count = 0; count < shares.length; count++) {
                if (formula == count)
                    continue; // If not the same value

                BigInteger startposition = shares[formula].getNumber();
                BigInteger nextposition = shares[count].getNumber();

                numerator = numerator.multiply((nextposition).negate()).mod(prime);
                denominator = denominator.multiply(startposition.subtract(nextposition)).mod(prime);
            }

            Element value = shares[formula].getShare();
            BigInteger tmp = (numerator).multiply(modInverse(denominator, prime));

            Element temEle = value.duplicate().mul(tmp);
            accum = accum.duplicate().add(temEle);
        }
        return accum;
    }

    private static BigInteger[] gcdD(BigInteger a, BigInteger b) {
        if (b.compareTo(BigInteger.ZERO) == 0)
            return new BigInteger[] { a, BigInteger.ONE, BigInteger.ZERO };
        else {
            BigInteger n = a.divide(b);
            BigInteger c = a.mod(b);
            BigInteger[] r = gcdD(b, c);
            return new BigInteger[] { r[0], r[2], r[1].subtract(r[2].multiply(n)) };
        }
    }

    private static BigInteger modInverse(BigInteger k, BigInteger prime) {
        k = k.mod(prime);
        BigInteger r = (k.compareTo(BigInteger.ZERO) == -1) ? (gcdD(prime, k.negate())[2]).negate() : gcdD(prime, k)[2];
        return prime.add(r).mod(prime);
    }


    
    
    /**
     * generate a random long identity
     *
     * @param input null
     * @return long
     */
    public static BigInteger randomBig(BigInteger mod) {
        Random rnd = new Random();
        long seed = System.nanoTime();
        rnd.setSeed(seed);
        BigInteger ranBig = new BigInteger(1024, rnd);
        ranBig = ranBig.mod(mod);
        return ranBig;
    }
    
    
    
    
    
}
