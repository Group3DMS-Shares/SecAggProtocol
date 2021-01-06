package edu.bjut.secaggcommon;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

public class RandomBigTest {

    @Test
    public void testRandom() throws NoSuchAlgorithmException, NoSuchProviderException {

        var s0 = getSecureRandom("hello");
        var s1 = getSecureRandom("hello");
        ArrayList<Integer> a1 = new ArrayList<>(Arrays.asList(s0.nextInt(), s0.nextInt()));
        ArrayList<Integer> a2 = new ArrayList<>(Arrays.asList(s1.nextInt(), s1.nextInt()));
        assertArrayEquals(a1.toArray(), a2.toArray());
    }

    private static final String ALGORITHM = "SHA1PRNG";
    private static final String PROVIDER = "SUN";

    private SecureRandom getSecureRandom(String seed) throws NoSuchAlgorithmException, NoSuchProviderException {
        SecureRandom sr = SecureRandom.getInstance(ALGORITHM, PROVIDER);
        sr.setSeed(seed.getBytes(StandardCharsets.UTF_8));
        return sr;
    }
}
