package edu.bjut.aaia.app;

import edu.bjut.common.util.Utils;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

public class UVShareTest {

    @Test
    public void testKeyAgreement() {
        Pairing pairing = PairingFactory.getPairing("aggVote1.properties");
        var g = pairing.getG1().newRandomElement().getImmutable();
        var order = pairing.getG1().getOrder();
        var sk1 = Utils.randomBig(order);
        var pk1 = g.mul(sk1);
        var sk2 = Utils.randomBig(order);
        var pk2 = g.mul(sk2);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        var commonLhs = pk2.mul(sk1);
        // var commonRhs = pk1.mul(sk2);
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
        // Assertions.assertEquals(commonLhs, commonRhs);
    }

    @Test
    public void testPairing() {
        Pairing pairing = PairingFactory.getPairing("aggVote1.properties");
        var g = pairing.getG1().newRandomElement().getImmutable();

        var order = pairing.getG1().getOrder();
        var e  = Utils.hash2ElementG1("test", pairing).getImmutable();
        var sk1 = Utils.randomBig(order);
        var pk1 = g.mul(sk1);
        var sk2 = Utils.randomBig(order);
        var pk2 = g.mul(sk2);
        var x = e.mul(sk1);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        var commonLhs = pairing.pairing(x, pk2);
        // var commonRhs = pairing.pairing(e.mul(sk2), pk1);
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
        // Assertions.assertEquals(commonLhs, commonRhs);
    }

}
