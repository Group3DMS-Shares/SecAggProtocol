package edu.bjut.crypto;

import org.bouncycastle.crypto.Digest;

public class PlainDigest implements Digest {

    private byte[] xBuf;
    private int xBufOff;
    private int byteCount;

    @Override
    public String getAlgorithmName() {
        return "Plaintxt";
    }

    @Override
    public int getDigestSize() {
        return byteCount;
    }

    @Override
    public void update(byte in) {
        xBuf[xBufOff++] = in;
        byteCount++;
    }

    @Override
    public void update(byte[] in, int inOff, int len) {
        while (len > 0) {
            update(in[inOff]);

            inOff++;
            len--;
        }
    }

    @Override
    public int doFinal(byte[] out, int outOff) {
        out[outOff++] = xBuf[outOff];
        return outOff - 1;
    }

    @Override
    public void reset() {
        byteCount = 0;
        xBufOff = 0;
        for (int i = 0; i < xBuf.length; i++)
        {
            xBuf[i] = 0;
        }
    }
    
}
