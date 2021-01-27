package edu.bjut.secaggcommon;

import org.junit.Test;

import edu.bjut.common.util.PRG;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class RandomBigTest {

    @Test
    public void testRandom() {

        PRG prg0 = new PRG("test");
        PRG prg1 = new PRG("test");
        BigInteger[] a = null;
        BigInteger[] b = null;
        try {
            a = prg0.genBigs(1000);
            b = prg1.genBigs(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<BigInteger> a1 = new ArrayList<BigInteger>(Arrays.asList(a));
        List<BigInteger> a2 = new ArrayList<BigInteger>(Arrays.asList(b));
        assertArrayEquals(a1.toArray(), a2.toArray());
    }
}
