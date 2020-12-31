package edu.bjut.psecagg.entity;

import edu.bjut.common.shamir.SecretShareBigInteger;

public class BetaShare {

    private long id;
    private SecretShareBigInteger betaShare;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public SecretShareBigInteger getBetaShare() {
        return betaShare;
    }

    public void setBetaShare(SecretShareBigInteger betaShare) {
        this.betaShare = betaShare;
    }

    public BetaShare(long id, SecretShareBigInteger betaShare) {
        this.id = id;
        this.betaShare = betaShare;
    }
}
