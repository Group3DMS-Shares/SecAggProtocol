package edu.bjut.psecagg.entity;

import edu.bjut.common.shamir.SecretShareBigInteger;

public class UVShare {
    private long uId;
    private long vId;
    private SecretShareBigInteger b_uShare;
    private SecretShareBigInteger skShare;

    public UVShare(long uId, long vId, SecretShareBigInteger b_uShare, SecretShareBigInteger skShare) {
        this.uId = uId;
        this.vId = vId;
        this.b_uShare = b_uShare;
        this.skShare = skShare;
    }

    public UVShare(byte[] bytes) {
    }

    public long getuId() {
        return uId;
    }

    public void setuId(long uId) {
        this.uId = uId;
    }

    public long getvId() {
        return vId;
    }

    public void setvId(long vId) {
        this.vId = vId;
    }

    public SecretShareBigInteger getB_uShare() {
        return b_uShare;
    }

    public void setB_uShare(SecretShareBigInteger b_uShare) {
        this.b_uShare = b_uShare;
    }

    public SecretShareBigInteger getSkShare() {
        return skShare;
    }

    public void setSkShare(SecretShareBigInteger skShare) {
        this.skShare = skShare;
    }

}
