package edu.bjut.message;

import it.unisa.dia.gas.jpbc.Element;

public class MessagePubKeys {
    private Element n_pK_n;
    private Element p_pK_n;
    private long idm;

    public Element getN_pK_n() {
        return n_pK_n;
    }

    public void setN_pK_n(Element n_pK_n) {
        this.n_pK_n = n_pK_n;
    }

    public Element getP_pK_n() {
        return p_pK_n;
    }

    public void setP_pK_n(Element p_pK_n) {
        this.p_pK_n = p_pK_n;
    }

    public MessagePubKeys(long idm, Element n_pK_n, Element p_pK_n) {
        this.idm = idm;
        this.n_pK_n = n_pK_n;
        this.p_pK_n = p_pK_n;
    }

    public long getIdm() {
        return idm;
    }

    public void setIdm(long idm) {
        this.idm = idm;
    }
    
}
