package edu.bjut.psecagg.app;

import java.math.BigInteger;
import org.junit.Test;

import edu.bjut.common.util.Utils;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        Pairing pairing = PairingFactory.getPairing("aggVote1.properties");

        BigInteger order = pairing.getG1().getOrder();
        Element g = pairing.getG1().newRandomElement().getImmutable();
        Element g1 = pairing.getG1().newRandomElement().getImmutable();
        System.out.println(g);
        System.out.println("g != g1--------");
        System.out.println(g1);
        BigInteger sk = Utils.randomBig(order);
        Element pk = g.mul(sk).getImmutable();
        Element pk2 = g.pow(sk).getImmutable();
        System.out.println(pk);
        System.out.println("g == g--------");
        System.out.println(pk2);
        Element add = pk.add(pk2);
        Element m = pk.mul(pk2);
        System.out.println(add);
        System.out.println("--------");
        System.out.println(m);
        BigInteger one = BigInteger.ONE;
        BigInteger two = BigInteger.TWO;
        BigInteger three = BigInteger.valueOf(3l);
        Element a = g.mul(one);
        Element b = g.mul(two);
        Element c = g.mul(three);
        Element d = a.mul(b);
        System.out.println(c);
        System.out.println("1 + 2 = 3--------");
        System.out.println(d);
        a = g.mul(one).mul(two);
        b = g.mul(two);
        System.out.println(a);
        System.out.println("mul --------");
        System.out.println(b);

    }
}
