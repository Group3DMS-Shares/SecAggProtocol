package edu.bjut.psecagg.messages;

import edu.bjut.psecagg.entity.CipherShare;

import java.util.ArrayList;

public class MsgRound1 {

    private int id;
    private ArrayList<CipherShare> ciperShares;

    public MsgRound1(int id, ArrayList<CipherShare> ciperShares) {
        this.id = id;
        this.ciperShares = ciperShares;
    }

    public ArrayList<CipherShare> getCiperShares() {
        return ciperShares;
    }

    public void setCiperShares(ArrayList<CipherShare> ciperShares) {
        this.ciperShares = ciperShares;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
