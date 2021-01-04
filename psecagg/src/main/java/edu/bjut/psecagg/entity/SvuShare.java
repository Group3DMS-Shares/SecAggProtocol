package edu.bjut.psecagg.entity;

import edu.bjut.common.shamir.SecretShareBigInteger;

public class SvuShare {

    private long id;
    private SecretShareBigInteger suvShare;

    public long getId() {
        return id;
    }

    public SecretShareBigInteger getSuvShare() {
        return suvShare;
    }

    public SvuShare(long id, SecretShareBigInteger suvShare) {
        this.id = id;
        this.suvShare = suvShare;
    }
}
