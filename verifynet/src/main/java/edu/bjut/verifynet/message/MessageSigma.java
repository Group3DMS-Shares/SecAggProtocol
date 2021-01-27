package edu.bjut.verifynet.message;

import edu.bjut.common.big.BigVec;

public class MessageSigma {

    private long id;
    private BigVec x_n_hat;
    // private Element An;
    // private Element Bn;
    // private Element Ln;
    // private Element Qn;
    // private BigInteger omega = BigInteger.ONE;

    public BigVec getX_n_hat() {
        return x_n_hat;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setX_n_hat(BigVec x_n_hat) {
        this.x_n_hat = x_n_hat;
    }

    public MessageSigma(BigVec x_n_hat) {
        this.x_n_hat = x_n_hat;
    }

}