package edu.bjut.psecagg.messages;

import edu.bjut.psecagg.entity.BetaShare;
import edu.bjut.psecagg.entity.UShare;

import java.util.ArrayList;

public class MsgRound4 {
    private ArrayList<BetaShare> betaShares;
    private ArrayList<UShare> uShares;

    public MsgRound4(ArrayList<BetaShare> betaShares, ArrayList<UShare> uShares) {
        this.betaShares = betaShares;
        this.uShares = uShares;
    }

    public ArrayList<BetaShare> getBetaShares() {
        return betaShares;
    }

    public void setBetaShares(ArrayList<BetaShare> betaShares) {
        this.betaShares = betaShares;
    }

    public ArrayList<UShare> getSvuShares() {
        return uShares;
    }

    public void setSvuShares(ArrayList<UShare> uShares) {
        this.uShares = uShares;
    }
}
