package edu.bjut.psecagg.messages;

import it.unisa.dia.gas.jpbc.Element;

public class MsgRound0 {

    private Element cPk_u;
    private Element sPk_u;
    private Element sigma_u;

    public MsgRound0(Element cPk_u, Element sPk_u, Element sigma_u) {
        this.setcPk_u(cPk_u);
        this.setsPk_u(sPk_u);
        this.setSigma_u(sigma_u);
    }

    public Element getcPk_u() {
        return cPk_u;
    }

    public void setcPk_u(Element cPk_u) {
        this.cPk_u = cPk_u;
    }

    public Element getsPk_u() {
        return sPk_u;
    }

    public void setsPk_u(Element sPk_u) {
        this.sPk_u = sPk_u;
    }

    public Element getSigma_u() {
        return sigma_u;
    }

    public void setSigma_u(Element sigma_u) {
        this.sigma_u = sigma_u;
    }
    
}
