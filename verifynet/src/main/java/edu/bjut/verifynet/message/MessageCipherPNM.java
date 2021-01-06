package edu.bjut.verifynet.message;

public class MessageCipherPNM {
    private long fromIdN;
    private long toIdM;
    private byte[] cFromIdN;
    private byte[] cToIdM;
    private byte[] betaNumber;
    private byte[] betaShare;
    private byte[] nSkNumber;
    private byte[] nSkShare;

    public MessageCipherPNM(long fromIdN, long toIdM, byte[] cFromIdN, byte[] cToIdM, byte[] betaNumber, byte[] betaShare, byte[] nSkNumber, byte[] nSkShare) {
        this.fromIdN = fromIdN;
        this.toIdM = toIdM;
        this.cFromIdN = cFromIdN;
        this.cToIdM = cToIdM;
        this.betaNumber = betaNumber;
        this.betaShare = betaShare;
        this.nSkNumber = nSkNumber;
        this.nSkShare = nSkShare;
    }

    public long getFromIdN() {
        return fromIdN;
    }

    public void setFromIdN(long fromIdN) {
        this.fromIdN = fromIdN;
    }

    public long getToIdM() {
        return toIdM;
    }

    public void setToIdM(long toIdM) {
        this.toIdM = toIdM;
    }

    public byte[] getcFromIdN() {
        return cFromIdN;
    }

    public void setcFromIdN(byte[] cFromIdN) {
        this.cFromIdN = cFromIdN;
    }

    public byte[] getcToIdM() {
        return cToIdM;
    }

    public void setcToIdM(byte[] cToIdM) {
        this.cToIdM = cToIdM;
    }

    public byte[] getBetaNumber() {
        return betaNumber;
    }

    public void setBetaNumber(byte[] betaNumber) {
        this.betaNumber = betaNumber;
    }

    public byte[] getBetaShare() {
        return betaShare;
    }

    public void setBetaShare(byte[] betaShare) {
        this.betaShare = betaShare;
    }

    public byte[] getnSkNumber() {
        return nSkNumber;
    }

    public void setnSkNumber(byte[] nSkNumber) {
        this.nSkNumber = nSkNumber;
    }

    public byte[] getnSkShare() {
        return nSkShare;
    }

    public void setnSkShare(byte[] nSkShare) {
        this.nSkShare = nSkShare;
    }
}
