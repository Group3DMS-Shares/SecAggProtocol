package edu.bjut.psecagg.app;

import edu.bjut.common.shamir.SecretShareBigInteger;
import edu.bjut.psecagg.entity.UVShare;
import org.junit.Test;

import java.math.BigInteger;

public class testUVShare {

    @Test
    public void testBigBytes() {
        BigInteger a = BigInteger.TEN;
        System.out.println(new BigInteger(a.toByteArray()));
    }

    @Test
    public void testToBytes() {
        var a = new SecretShareBigInteger(BigInteger.ONE, BigInteger.TEN);
        var b = new SecretShareBigInteger(BigInteger.ONE, BigInteger.TEN);
        var uv = new UVShare(0, 1, a, b);
        byte[] bytes = uv.getBytes();
        var uvS = new UVShare(bytes);
    }
}
