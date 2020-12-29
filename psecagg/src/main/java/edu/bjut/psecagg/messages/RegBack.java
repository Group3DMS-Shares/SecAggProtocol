package edu.bjut.psecagg.messages;

import java.util.ArrayList;
import it.unisa.dia.gas.jpbc.Element;

public class RegBack {
    private ArrayList<Element> alKeys;
    private Element ri1;
    private Element riP1;
    public ArrayList<Element> getAlKeys() {
        return alKeys;
    }
    public void setAlKeys(ArrayList<Element> alKeys) {
        this.alKeys = alKeys;
    }
    public Element getRi1() {
        return ri1;
    }
    public void setRi1(Element ri1) {
        this.ri1 = ri1;
    }
    public Element getRiP1() {
        return riP1;
    }
    public void setRiP1(Element riP1) {
        this.riP1 = riP1;
    }
    public RegBack(ArrayList<Element> alKeys, Element ri1, Element riP1) {
        super();
        this.alKeys = alKeys;
        this.ri1 = ri1;
        this.riP1 = riP1;
    }
    
}
