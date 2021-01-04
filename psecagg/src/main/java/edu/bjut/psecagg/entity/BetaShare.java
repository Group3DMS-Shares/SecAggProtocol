package edu.bjut.psecagg.entity;

import edu.bjut.common.shamir.SecretShareBigInteger;

public class BetaShare {

    private long id;
    private SecretShareBigInteger betaShare;

    public long getId() {
        return id;
    }

    public SecretShareBigInteger getBetaShare() {
        return betaShare;
    }

    public BetaShare(long id, SecretShareBigInteger betaShare) {
        this.id = id;
        this.betaShare = betaShare;
    }
}
