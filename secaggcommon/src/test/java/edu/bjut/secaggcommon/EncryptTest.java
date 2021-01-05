package edu.bjut.secaggcommon;

import edu.bjut.common.aes.AesCipher;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.junit.Test;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;

public class EncryptTest {

    @Test
    public void testAES() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
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

        AesCipher aesCipher = new AesCipher(key);
        assertEquals(plainStr, new String(aesCipher.decrypt(aesCipher.encrypt(plainStr.getBytes()))));
    }
}
