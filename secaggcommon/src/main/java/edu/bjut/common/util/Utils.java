package edu.bjut.common.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Random;

// import edu.bjut.psecagg.entity.Params;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class Utils {

    /**
     * Hashing 2 a big number mod by a public parameter
     *
     * @param orgStr String id, BigInteger xi, Element ci, BigInteger di, long ti
     * @return BigInteger
     */
    public static BigInteger hash2Big(String orgStr, BigInteger order) {
        BigInteger bi = new BigInteger(orgStr.getBytes());
        bi = bi.mod(order);
        return bi;
    }
    
    /**
     * generate a random long identity
     *
     * @return long
     */
    public static long randomlong() {
        Random rnd = new Random();
        long seed = System.nanoTime();
        rnd.setSeed(seed);
        return rnd.nextLong();
    }

    /**
     * generate a random int number that is less than 999
     *
     * @param num null
     * @return int
     */
    public static int randomInt(int num) {
        Random rnd = new Random();
        long seed = System.nanoTime();
        rnd.setSeed(seed);
        return rnd.nextInt(num);
    }

    /**
     * generate a random int number that is less than 999
     *
     * @param fail null
     * @return int
     */
    public static boolean[] setFailedParticipants(int fail, int length) {
        boolean[] results = new boolean[length];
        if (fail >= length) {
            for (var i = 0; i < fail; ++i) results[i] = true;
        } else {
            Random rnd = new Random();
            while (fail > 0) {
                int index = rnd.nextInt(length - 1);
                if (!results[index]) {
                    results[index] = true;
                    --fail;
                }
            }
        }
        return results;
    }

    /**
     * Hashing a string to an Element in the Elliptic Curve
     *
     * @param originalString String originalString
     * @return Element Element of G1
     */
    public static Element hash2ElementG1(String originalString, Pairing pairing) {
        byte[] oiginalBytes = originalString.getBytes(StandardCharsets.UTF_8);
        return pairing.getG1().newElementFromHash(oiginalBytes, 0, oiginalBytes.length);
    }


    /**
     * generate a random long identity
     *
     * @param mod null
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

    static BigInteger smallMod = new BigInteger("1152921504606846976");
    /**
     * generate a random long identity
     *
     * @return long
     */
    public static BigInteger randomFai() {

        Random rnd = new Random();
        long seed = System.nanoTime();
        rnd.setSeed(seed);
        BigInteger ranBig = new BigInteger(61, rnd);
        ranBig = ranBig.mod(Utils.smallMod);
        return ranBig;
    }

    public static BigInteger[] vecAdd(BigInteger[] a, BigInteger[] b) {
        int len = a.length;
        BigInteger[] c = new BigInteger[len];
        for (int i = 0; i < len; ++i) {
            c[i] = a[i].add(b[i]);
        }
        return c;
    }

    public static BigInteger[] vecSubtract(BigInteger[] a, BigInteger[] b) {
        int len = a.length;
        BigInteger[] c = new BigInteger[len];
        for (int i = 0; i < len; ++i) {
            c[i] = a[i].subtract(b[i]);
        }
        return c;
    }

    private static int count = 0;

    public static int incrementId() {
        return count++;
    }
}
