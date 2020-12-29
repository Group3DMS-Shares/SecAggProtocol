package edu.bjut.psecagg.messages;

import it.unisa.dia.gas.jpbc.Element;

public class RegMessage {
    private long id;
    private Element key;
    private Element qi;
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public Element getKey() {
        return key;
    }
    public void setKey(Element key) {
        this.key = key;
    }
    public Element getQi() {
        return qi;
    }
    public void setQi(Element qi) {
        this.qi = qi;
    }
    public RegMessage(long id, Element key, Element qi) {
        super();
        this.id = id;
        this.key = key;
        this.qi = qi;
    }
    
}
