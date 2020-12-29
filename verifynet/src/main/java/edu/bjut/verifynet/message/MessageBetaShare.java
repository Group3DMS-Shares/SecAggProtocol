package edu.bjut.verifynet.message;

import edu.bjut.common.Shamir.SecretShareBigInteger;

public class MessageBetaShare {
    private long toId;
    private SecretShareBigInteger betaNtoM;

    public MessageBetaShare(long toId, SecretShareBigInteger betaNtoM) {
        this.setToId(toId);
        this.betaNtoM = betaNtoM;
    }

    public SecretShareBigInteger getBetaNtoM() {
        return betaNtoM;
    }

    public long getToId() {
        return toId;
    }

    public void setToId(long toId) {
        this.toId = toId;
    }


    public void setBetaNtoM(SecretShareBigInteger betaNtoM) {
        this.betaNtoM = betaNtoM;
    }
}
