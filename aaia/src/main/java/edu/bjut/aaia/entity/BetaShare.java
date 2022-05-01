package edu.bjut.aaia.entity;

import edu.bjut.common.shamir.SecretShareBigInteger;

public class BetaShare {

    private int id;
    private SecretShareBigInteger betaShare;

    public int getId() {
        return id;
    }

    public SecretShareBigInteger getBetaShare() {
        return betaShare;
    }

    public BetaShare(int id, SecretShareBigInteger betaShare) {
        this.id = id;
        this.betaShare = betaShare;
    }
}
