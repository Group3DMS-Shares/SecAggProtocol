package edu.bjut;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import edu.bjut.entity.MessageKeys;
import edu.bjut.entity.Params;
import edu.bjut.entity.ParamsECC;
import edu.bjut.entity.TA;
import edu.bjut.entity.User;
import edu.bjut.util.Utils;
import it.unisa.dia.gas.jpbc.Element;


public class TATest {

    private TA ta;
    private ArrayList<User> users;

    @Before
    public void constructSystem() {
        ta = new TA();
        users = new ArrayList<User>();
        for (int i = 0; i < Params.PARTICIPANT_NUM ; ++i)  {
            User u = new User();
            MessageKeys msgKeys = ta.genUserKeyPair();
            u.setKeys(msgKeys);
            u.setParamsECC(ta.getParamsECC());
            users.add(u);
        }
    }

    @Test
    public void testECDH() {
        // test ta ecdh
        MessageKeys msgKey1 = ta.genUserKeyPair();
        MessageKeys msgKey2 = ta.genUserKeyPair();

        Element sharem1Tom2 = msgKey1.getN_pK_n().mul(msgKey2.getN_sK_n());
        Element sharem2Tom1 = msgKey2.getN_pK_n().mul(msgKey1.getN_sK_n());
        System.out.println("testShare1To2: " + sharem1Tom2.toString());
        System.out.println("testShare2To1: " + sharem2Tom1.toString());

        // test user ecdh
        User u1 = users.get(0);
        User u2 = users.get(1);
        BigInteger u1Sk = u1.getN_sK_n();
        BigInteger u2Sk = u2.getN_sK_n();

        Element u1Pk = u1.getN_pK_n();
        Element u2Pk = u2.getN_pK_n();
        Element share1To2 = u1Pk.mul(u2Sk);
        Element share2To1 = u2Pk.mul(u1Sk);
        
        System.out.println("testShare1To2: " + share1To2.toString());
        System.out.println("testShare2To1: " + share2To1.toString());
        assertTrue(share1To2.isEqual(share2To1));
    }

    @Test
    public void testModReverse() {
        ParamsECC paramsECC = ta.getParamsECC();
        Element g = paramsECC.getGeneratorOfG1();
        System.out.println(g);
        BigInteger order = paramsECC.getPairing().getG1().getOrder();
        BigInteger d = Utils.randomBig(order);
        BigInteger d_inverse = d.modInverse(order);
        System.out.println(d_inverse);
        Element r = g.mul(d).mul(d_inverse);
        System.out.println(r);
    }
    
}
