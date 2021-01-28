package edu.bjut.aggprotocol.messages;

import edu.bjut.common.big.BigVec;
import it.unisa.dia.gas.jpbc.Element;

public class RepMessage {
    private long id;
    private BigVec ci;
    private Element si;
    private int ti;
    public RepMessage(long id, BigVec ci, Element si, int ti) {
        super();
        this.id = id;
        this.ci = ci;
        this.si = si;
        this.ti = ti;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public BigVec getCi() {
        return ci;
    }
    public void setCi(BigVec ci) {
        this.ci = ci;
    }
    public Element getSi() {
        return si;
    }
    public void setSi(Element si) {
        this.si = si;
    }
    public int getTi() {
        return ti;
    }
    public void setTi(int ti) {
        this.ti = ti;
    }

}