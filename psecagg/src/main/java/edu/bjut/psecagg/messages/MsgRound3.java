package edu.bjut.psecagg.messages;

import it.unisa.dia.gas.jpbc.Element;

public class MsgRound3 {
    private long id;
    private Element signature;

    public MsgRound3(long id, Element signature) {
        this.id = id;
        this.signature = signature;
    }

    public MsgRound3(Element signature) {
        this.signature = signature;
    }

    public Element getSignature() {
        return signature;
    }

    public void setSignature(Element signature) {
        this.signature = signature;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
