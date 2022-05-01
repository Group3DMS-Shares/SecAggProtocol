package edu.bjut.secaggcommon;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;

import edu.bjut.common.aes.AesCipher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EncryptTest {

    @Test
    public void testAES() throws Exception {
        String key = "testKey";
        String plainStr = "hello world!";

        // digest
        Digest digest = new SHA256Digest();
        byte[] keyText = key.getBytes();
        digest.update(keyText, 0, keyText.length );
        byte[] digestBytes = new byte[digest.getDigestSize()];
        int out = digest.doFinal(digestBytes, 0);
        assertEquals(digest.getDigestSize(), out);

        // encrypt
        SecretKey secretKey = new SecretKeySpec(digestBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] cipherBytes = cipher.doFinal(plainStr.getBytes());

        // decrypt
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] plainBytes = cipher.doFinal(cipherBytes);
        assertEquals(plainStr, new String(plainBytes));

        AesCipher aesCipher = new AesCipher(key, Cipher.ENCRYPT_MODE);

        byte[] cipherBytes1 = aesCipher.encrypt(plainStr.getBytes());
        AesCipher aesCipher1 = new AesCipher(key, Cipher.DECRYPT_MODE);
        byte[] plainBytes1 = aesCipher1.decrypt(cipherBytes1);
        assertEquals(plainStr, new String(plainBytes1));
    }
}
