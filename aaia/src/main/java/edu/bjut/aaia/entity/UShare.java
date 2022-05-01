package edu.bjut.aaia.entity;

import edu.bjut.common.shamir.SecretShareBigInteger;

public class UShare {

    private int id;
    private SecretShareBigInteger uShare;

    public int getId() {
        return id;
    }

    public SecretShareBigInteger getUShare() {
        return uShare;
    }

    public UShare(int id, SecretShareBigInteger uShare) {
        this.id = id;
        this.uShare = uShare;
    }
}
