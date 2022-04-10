package edu.bjut.aggprotocol.messages;

import edu.bjut.common.shamir.SecretShareBigInteger;

public class ShareBuMsg {
    private long id;
    private SecretShareBigInteger[] shares;

    public ShareBuMsg(long id, SecretShareBigInteger[] shares) {
        this.setId(id);
        this.setShares(shares);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public SecretShareBigInteger[] getShares() {
        return shares;
    }

    public void setShares(SecretShareBigInteger[] shares) {
        this.shares = shares;
    }

}
