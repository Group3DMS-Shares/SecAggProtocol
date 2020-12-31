package edu.bjut.psecagg.app;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.junit.Test;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
//import org.apache.codec.binary.Hex.encodeHexString(byteArray);

public class EncryptTest {

    @Test
    public void testAES() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        String key = "testKeyTestKey!!";
        String plainStr = "hello world!";

        // digest
        Digest digest = new SHA256Digest();
        byte[] plaintext = plainStr.getBytes();
        digest.update(plaintext, 0, plaintext.length );
        byte[] digestBytes = new byte[digest.getDigestSize()];
        int out = digest.doFinal(digestBytes, 0);
        System.out.println(out);
        System.out.println(Hex.encodeHexString(digestBytes));
        // encrypt
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] cipherBytes = cipher.doFinal(digestBytes);

        // decrypt
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] plainBytes = cipher.doFinal(cipherBytes);
        System.out.println(Hex.encodeHexString(plainBytes));
    }
}
