package edu.bjut.common.messages;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class ParamsECC {
    
    private Pairing pairing;
    private Element generatorOfG1;
    
    public ParamsECC(Pairing pairing, Element generatorOfG1) {
        super();
        this.pairing = pairing;
        this.generatorOfG1 = generatorOfG1;
    }

    public Pairing getPairing() {
        return pairing;
    }
    public void setPairing(Pairing pairing) {
        this.pairing = pairing;
    }

    public Element getGeneratorOfG1() {
        return generatorOfG1;
    }

    public void setGeneratorOfG1(Element generatorOfG1) {
        this.generatorOfG1 = generatorOfG1;
    }
    
}
