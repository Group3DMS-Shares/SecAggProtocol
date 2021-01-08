package edu.bjut.aggprotocol.messages;

import java.util.ArrayList;
import it.unisa.dia.gas.jpbc.Element;

public class RegBack {
    private ArrayList<Element> allKeys;
    private Element ri1Pre;
    private Element riP1Next;
    public ArrayList<Element> getAlKeys() {
        return allKeys;
    }
    public void setAlKeys(ArrayList<Element> alKeys) {
        this.allKeys = alKeys;
    }
    public Element getRi1() {
        return ri1Pre;
    }
    public void setRi1(Element ri1) {
        this.ri1Pre = ri1;
    }
    public Element getRiP1() {
        return riP1Next;
    }
    public void setRiP1(Element riP1) {
        this.riP1Next = riP1;
    }
    public RegBack(ArrayList<Element> alKeys, Element ri1, Element riP1) {
        this.allKeys = alKeys;
        this.ri1Pre = ri1;
        this.riP1Next = riP1;
    }
    
}
