package edu.bjut.secaggcommon;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    @Test
    public void modInverse() {
        System.out.println(BigInteger.valueOf(2).modInverse(BigInteger.valueOf(11)));
        Pairing pairing = PairingFactory.getPairing("aggVote1.properties");
        BigInteger order = pairing.getG1().getOrder();
        Element g = pairing.getG1().newRandomElement().getImmutable();
        pairing.getG1().newZeroElement();
        Element neg_g = g.negate();
        Element zero = g.add(neg_g).getImmutable();
        Element x = g.mul(BigInteger.valueOf(10));
        System.out.println(g);
        System.out.println(zero.add(x));
        System.out.println(x);
    }
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        Pairing pairing = PairingFactory.getPairing("aggVote1.properties");

        Element g = pairing.getG1().newRandomElement().getImmutable();

        var  three = BigInteger.valueOf(3);
        var handred = BigInteger.valueOf(100);
        Element a = g.mul(handred).getImmutable();
        Element b = g.mul(three).getImmutable();
        System.out.println("postive minus");
        System.out.println(a.div(b));
        System.out.println(g.mul(BigInteger.valueOf(97)));
        System.out.println("negtive minus");
        System.out.println(b.sub(a).mul(BigInteger.TWO));
        System.out.println(g.mul(three).mul(BigInteger.TWO).add(g.negate().mul(handred).mul(BigInteger.TWO)));
        
        Element pk1 = g.mul(BigInteger.TWO);
        Element pk2 = g.mul(BigInteger.valueOf(3));
        Element pk3 = g.mul(BigInteger.TEN);
        // compute X_1
        Element z1_i = pk3.getImmutable().mul(BigInteger.TWO);
        Element z1_i1 = pk2.getImmutable().mul(BigInteger.TWO);
        Element x_1 = z1_i1.getImmutable().sub(z1_i);

        Element x_1_r = g.mul(BigInteger.valueOf(6)).add(g.negate().mul(BigInteger.valueOf(20)));
        Element z1_i_r = g.mul(BigInteger.valueOf(20));
        Element z1_i1_r = g.mul(BigInteger.valueOf(6));
        System.out.println("z_i");
        System.out.println(z1_i);
        System.out.println(z1_i_r);
        System.out.println(z1_i1);
        System.out.println(z1_i1_r);

        System.out.println("x_1 ->");
        System.out.println(x_1);
        System.out.println(x_1_r);
        System.out.println(x_1_r.mul(BigInteger.ONE));

        Element z2_i = pk1.getImmutable().mul(BigInteger.valueOf(3));
        Element z2_i1 = pk3.getImmutable().mul(BigInteger.valueOf(3));
        Element x_2 = z2_i1.getImmutable().sub(z2_i);
        Element x_2_r = g.mul(BigInteger.valueOf(30)).add(g.negate().mul(BigInteger.valueOf(6)));
        System.out.println("x_2 ->");
        System.out.println(x_2);
        System.out.println(x_2_r);

        Element z3_i = pk2.getImmutable().mul(BigInteger.TEN);
        Element z3_i1 = pk1.getImmutable().mul(BigInteger.TEN);
        Element x_3 = z3_i1.getImmutable().sub(z3_i);

        List<Element> list = new ArrayList<>();
        list.add(x_1);
        list.add(x_2);
        list.add(x_3);
        System.out.println("======");
        List<Element> list2 = new ArrayList<>();
        for (int index = 0; index < 2; ++index) {
            int c = 2;
            Element ti = pairing.getG1().newOneElement();
            for (int i = 0; i < 2; i++) {
                int next = (index + i) % list.size();
                System.out.println("index: " + next + ", value: " + c);
                Element tem = list.get(next).duplicate().mul(BigInteger.valueOf(c--));
                ti = ti.duplicate().mul(tem);
            }
            list2.add(ti);
        }

        Element tem = z1_i.getImmutable().mul(BigInteger.valueOf(3));
        System.out.println(tem.mul(list2.get(0)));
        Element tem1 = z2_i.getImmutable().mul(BigInteger.valueOf(3));
        System.out.println(tem1.mul(list2.get(1)));
        System.out.println("final");
        {
            Element z0 = g.mul(BigInteger.valueOf(20));
            Element z = g.mul(BigInteger.valueOf(6)).add(g.negate().mul(BigInteger.valueOf(20)));
            Element z2 = g.mul(BigInteger.valueOf(30)).add(g.negate().mul(BigInteger.valueOf(6)));
            Element y = z.mul(BigInteger.valueOf(2)).mul(z2);
            System.out.println(z0.mul(three).mul(y));
        }

        {
            System.out.println("case 1:");
            Element z0 = g.mul(BigInteger.valueOf(60));
            Element z = g.mul(BigInteger.valueOf(6)).add(g.negate().mul(BigInteger.valueOf(20)));
            Element z2 = g.mul(BigInteger.valueOf(30)).add(g.negate().mul(BigInteger.valueOf(6)));
            Element y = z.mul(BigInteger.valueOf(2)).mul(z2);
            System.out.println(z0.mul(y));

        }

        {
            System.out.println("case 2:");
            Element z0 = g.mul(BigInteger.valueOf(60));
            Element z = g.mul(BigInteger.valueOf(12)).add(g.negate().mul(BigInteger.valueOf(40)));
            Element z2 = g.mul(BigInteger.valueOf(30)).add(g.negate().mul(BigInteger.valueOf(6)));
            Element y = z.mul(z2);
            System.out.println(z0.mul(y));
        }

        {
            System.out.println("case 3:");
            Element z0 = g.mul(BigInteger.valueOf(60));
            Element z = g.mul(BigInteger.valueOf(12)).mul(g.negate().mul(BigInteger.valueOf(40)));
            Element z2 = g.mul(BigInteger.valueOf(30)).mul(g.negate().mul(BigInteger.valueOf(6)));
            Element y = z.mul(z2);
            Element z_sub = g.mul(BigInteger.valueOf(102)).mul(g.negate().mul(BigInteger.valueOf(46)));
            System.out.println(z0.mul(y));
            System.out.println(z_sub);
            System.out.println(g.mul(BigInteger.valueOf(102-46)));
        }

        {
            System.out.println("case 4:");
            Element share1 = pk1.mul(BigInteger.TEN);
            Element share2 = pk2.mul(BigInteger.TWO);
            Element share3 = pk3.mul(BigInteger.valueOf(3));
            System.out.println(share1.add(share2).add(share3));
        }
    }
}
