package edu.bjut.common.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

public class PRG {
    private static final String ALGORITHM = "SHA1PRNG";
    private static final String PROVIDER = "SUN";

    private String seed;
    private int nBits = 128;

    public PRG(String seed) {
        this.seed = seed;
    }

    public PRG(String seed, int nBits) {
        this.seed = seed;
        this.nBits = nBits;
    }

    private SecureRandom getSecureRandom() throws NoSuchAlgorithmException, NoSuchProviderException {
        SecureRandom sr = SecureRandom.getInstance(ALGORITHM, PROVIDER);
        sr.setSeed(this.seed.getBytes(StandardCharsets.UTF_8));
        return sr;
    }

    public BigInteger[] genBigs(int len) throws NoSuchAlgorithmException, NoSuchProviderException {
        SecureRandom rand = getSecureRandom();
        BigInteger[] v = new BigInteger[len];
        for (int i = 0; i < len; ++i) {
            v[i] = new BigInteger(this.nBits, rand);
        }
        return v;
    }
    
    
}
