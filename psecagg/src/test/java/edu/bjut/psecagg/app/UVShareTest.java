package edu.bjut.psecagg.app;

import org.junit.Test;

import java.math.BigInteger;

public class UVShareTest {

    @Test
    public void testBigBytes() {
        BigInteger a = BigInteger.TEN;
        System.out.println(new BigInteger(a.toByteArray()));
    }

}
