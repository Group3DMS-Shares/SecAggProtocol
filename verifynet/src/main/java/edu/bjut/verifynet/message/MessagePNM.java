package edu.bjut.verifynet.message;

import edu.bjut.common.shamir.SecretShareBigInteger;

public class MessagePNM {
    private long fromIdN;
    private long toIdM;
    private SecretShareBigInteger nSkNM;
    private SecretShareBigInteger betaNM;

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

    public SecretShareBigInteger getnSkNM() {
        return nSkNM;
    }

    public void setnSkNM(SecretShareBigInteger nSkNM) {
        this.nSkNM = nSkNM;
    }

    public SecretShareBigInteger getBetaNM() {
        return betaNM;
    }

    public void setBetaNM(SecretShareBigInteger betaNM) {
        this.betaNM = betaNM;
    }

    public MessagePNM(long fromIdN, long toIdM, SecretShareBigInteger nskNM, SecretShareBigInteger betaNM) {
        this.fromIdN = fromIdN;
        this.toIdM = toIdM;
        this.nSkNM = nskNM;
        this.betaNM = betaNM;
    }

}
