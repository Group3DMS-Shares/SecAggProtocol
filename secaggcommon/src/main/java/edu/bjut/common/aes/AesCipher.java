package edu.bjut.common.aes;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;

public class AesCipher {

    protected static int BLOCK_SIZE_BITS = 16;
    private SecretKey keySpec;
    private Cipher cipher;
    byte[] iv = new byte[AesCipher.BLOCK_SIZE_BITS];

    public AesCipher(String keyString, int mode) throws Exception {
        Digest digest = new SHA256Digest();
        byte[] keyText = keyString.getBytes();
        digest.update(keyText, 0, keyText.length);
        byte[] digestBytes = new byte[digest.getDigestSize()];
        this.keySpec = new SecretKeySpec(digestBytes, "AES");
        this.cipher = Cipher.getInstance("AES/CTR/NoPadding");
        this.cipher.init(mode, keySpec, new IvParameterSpec(iv));
    }

    public byte[] encrypt(byte[] plaintext) throws BadPaddingException, IllegalBlockSizeException {
        byte[] cipherBytes = cipher.doFinal(plaintext);
        return cipherBytes;
    }

    public byte[] decrypt(byte[] ciphertext) throws BadPaddingException, IllegalBlockSizeException {
        byte[] plainBytes = cipher.doFinal(ciphertext);
        return plainBytes;
    }
}
