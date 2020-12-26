package edu.bjut.entity;

import edu.bjut.Shamir.SecretShareBigInteger;

public class MessageAgg {
    private long fromId;
    private long toId;
    private SecretShareBigInteger betaNtoM;
    private SecretShareBigInteger nsKm_NtoM;

    public MessageAgg(long fromId, long toId, SecretShareBigInteger betaNtoM, SecretShareBigInteger nsKm_NtoM) {
        this.fromId = fromId;
        this.setToId(toId);
        this.betaNtoM = betaNtoM;
        this.nsKm_NtoM = nsKm_NtoM;
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

    public SecretShareBigInteger getNsKm_NtoM() {
        return nsKm_NtoM;
    }

    public void setNsKm_NtoM(SecretShareBigInteger nsKm_NtoM) {
        this.nsKm_NtoM = nsKm_NtoM;
    }

    public void setBetaNtoM(SecretShareBigInteger betaNtoM) {
        this.betaNtoM = betaNtoM;
    }

    public long getFromId() {
        return fromId;
    }

    public void setFromId(long fromId) {
        this.fromId = fromId;
    }

}
