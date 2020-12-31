package edu.bjut.verifynet.message;

import edu.bjut.common.shamir.SecretShareBigInteger;

public class MessageDroupoutShare {
    private long id;
    private SecretShareBigInteger nsKm_NtoM;

    public MessageDroupoutShare(long id, SecretShareBigInteger nsKm_NtoM) {
        this.setId(id);
        this.nsKm_NtoM = nsKm_NtoM;
    }

    public SecretShareBigInteger getNsKm_NtoM() {
        return nsKm_NtoM;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setNsKm_NtoM(SecretShareBigInteger nsKm_NtoM) {
        this.nsKm_NtoM = nsKm_NtoM;
    }

}
