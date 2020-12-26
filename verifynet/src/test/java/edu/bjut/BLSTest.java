package edu.bjut;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.junit.Test;

import edu.bjut.crypto.BLS01;

public class BLSTest {

    @Test
    public void testEnc() {
        BLS01 bls01 = new BLS01();
        // Setup
        AsymmetricCipherKeyPair keyPair = bls01.keyGen(bls01.setup());

        // Test same message
        String message = "Hello World!";
        assertTrue(bls01.verify(bls01.sign(message, keyPair.getPrivate()), message, keyPair.getPublic()));
        // Test different messages
        assertFalse(bls01.verify(bls01.sign(message, keyPair.getPrivate()), "Hello Italy!", keyPair.getPublic()));
        bls01.encrypt(message, keyPair.getPrivate());

    }
    
}
