package edu.bjut.verifynet.messages;

import it.unisa.dia.gas.jpbc.Element;

public class MsgRound3 {
    private long id;
    private Element signature;

    public MsgRound3(long id, Element signature) {
        this.id = id;
        this.signature = signature;
    }

    public Element getSignature() {
        return signature;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
