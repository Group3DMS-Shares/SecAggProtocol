package edu.bjut.psecagg.messages;

import edu.bjut.psecagg.entity.BetaShare;
import edu.bjut.psecagg.entity.SvuShare;

import java.util.ArrayList;

public class MsgRound4 {
    private ArrayList<BetaShare> betaShares;
    private ArrayList<SvuShare> svuShares;

    public MsgRound4(ArrayList<BetaShare> betaShares, ArrayList<SvuShare> svuShares) {
        this.betaShares = betaShares;
        this.svuShares = svuShares;
    }

    public ArrayList<BetaShare> getBetaShares() {
        return betaShares;
    }

    public void setBetaShares(ArrayList<BetaShare> betaShares) {
        this.betaShares = betaShares;
    }

    public ArrayList<SvuShare> getSvuShares() {
        return svuShares;
    }

    public void setSvuShares(ArrayList<SvuShare> svuShares) {
        this.svuShares = svuShares;
    }
}
