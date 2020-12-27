package edu.bjut.message;

import java.math.BigInteger;

import it.unisa.dia.gas.jpbc.Element;

public class MessageSigma {

    private long id;
    private BigInteger x_n_hat;
    private Element An;
    private Element Bn;
    private Element Ln;
    private Element Qn;
    private BigInteger omega = BigInteger.ONE;

    public BigInteger getX_n_hat() {
        return x_n_hat;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setX_n_hat(BigInteger x_n_hat) {
        this.x_n_hat = x_n_hat;
    }

    public MessageSigma(BigInteger x_n_hat) {
        this.x_n_hat = x_n_hat;
    }

}