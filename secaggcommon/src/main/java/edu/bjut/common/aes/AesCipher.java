package edu.bjut.common.aes;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AesCipher {

    private SecretKey key;
    private Cipher cipher;

    public AesCipher(String keyString) throws NoSuchPaddingException, NoSuchAlgorithmException {
        Digest digest = new SHA256Digest();
        byte[] keyText = keyString.getBytes();
        digest.update(keyText, 0, keyText.length );
        byte[] digestBytes = new byte[digest.getDigestSize()];
        this.key = new SecretKeySpec(digestBytes, "AES");
        this.cipher = Cipher.getInstance("AES");
    }

    public byte[] encrypt(byte[] plaintext) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherBytes = cipher.doFinal(plaintext);
        return cipherBytes;
    }

    public byte[] decrypt(byte[] ciphertext) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] plainBytes = cipher.doFinal(ciphertext);
        return plainBytes;
    }
}
