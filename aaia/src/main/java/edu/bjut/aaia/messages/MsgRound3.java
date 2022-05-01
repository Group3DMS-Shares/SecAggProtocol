package edu.bjut.aaia.messages;

import it.unisa.dia.gas.jpbc.Element;

public class MsgRound3 {
    private int id;
    private Element signature;

    public MsgRound3(int id, Element signature) {
        this.id = id;
        this.signature = signature;
    }

    public Element getSignature() {
        return signature;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
