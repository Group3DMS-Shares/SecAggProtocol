package edu.bjut.psecagg.entity;

import edu.bjut.common.shamir.SecretShareBigInteger;

public class UShare {

    private long id;
    private SecretShareBigInteger uShare;

    public long getId() {
        return id;
    }

    public SecretShareBigInteger getUShare() {
        return uShare;
    }

    public UShare(long id, SecretShareBigInteger uShare) {
        this.id = id;
        this.uShare = uShare;
    }
}
