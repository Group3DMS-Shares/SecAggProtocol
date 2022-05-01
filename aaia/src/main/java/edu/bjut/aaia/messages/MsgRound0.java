package edu.bjut.aaia.messages;

import it.unisa.dia.gas.jpbc.Element;

public class MsgRound0 {

    private int id;
    private Element cPk_u;
    private Element sPk_u;
    private Element sigma_u;

    public MsgRound0(int id, Element cPk_u, Element sPk_u, Element sigma_u) {
        this.setId(id);
        this.setcPk_u(cPk_u);
        this.setsPk_u(sPk_u);
        this.setSigma_u(sigma_u);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
