package edu.bjut.secaggcommon;


import edu.bjut.common.big.BigVec;
import edu.bjut.common.util.PRG;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class RandomBigTest {

    @Test
    public void testRandom() {
        String seed = "test";
        String seed1 = "test";
        int bSize = 10;
        PRG prg0 = new PRG(seed);
        PRG prg1 = new PRG(seed);
        BigInteger[] a = null;
        BigInteger[] b = null;
        try {
            a = prg0.genBigs(bSize);
            b = prg1.genBigs(bSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<BigInteger> a1 = new ArrayList<BigInteger>(Arrays.asList(a));
        List<BigInteger> a2 = new ArrayList<BigInteger>(Arrays.asList(b));
        assertArrayEquals(a1.toArray(), a2.toArray());
        var b1 = BigVec.genPRGBigVec(seed, bSize);
        var b2 = BigVec.genPRGBigVec(seed, bSize);
        
        assertEquals(seed, seed1);
        assertEquals(b1, b2);
    }
}
