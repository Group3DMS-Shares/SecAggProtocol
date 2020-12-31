package edu.bjut.verifynet.message;

import edu.bjut.common.shamir.SecretShareBigInteger;

public class MessagePNM {
    private long fromIdN;
    private long ToIdM;
    private SecretShareBigInteger N_Sk_n_m;
    private SecretShareBigInteger beta_n_m;

    public long getFromIdN() {
        return fromIdN;
    }

    public void setFromIdN(long fromIdN) {
        this.fromIdN = fromIdN;
    }

    public long getToIdM() {
        return ToIdM;
    }

    public void setToIdM(long toIdM) {
        ToIdM = toIdM;
    }

    public SecretShareBigInteger getN_Sk_n_m() {
        return N_Sk_n_m;
    }

    public void setN_Sk_n_m(SecretShareBigInteger n_Sk_n_m) {
        N_Sk_n_m = n_Sk_n_m;
    }

    public SecretShareBigInteger getBeta_n_m() {
        return beta_n_m;
    }

    public void setBeta_n_m(SecretShareBigInteger beta_n_m) {
        this.beta_n_m = beta_n_m;
    }

    public MessagePNM(long fromIdN, long toIdM, SecretShareBigInteger n_Sk_n_m, SecretShareBigInteger beta_n_m) {
        this.fromIdN = fromIdN;
        ToIdM = toIdM;
        N_Sk_n_m = n_Sk_n_m;
        this.beta_n_m = beta_n_m;
    }

}
