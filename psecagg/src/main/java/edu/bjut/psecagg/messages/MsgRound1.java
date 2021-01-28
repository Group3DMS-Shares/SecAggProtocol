package edu.bjut.psecagg.messages;

import edu.bjut.psecagg.entity.CipherShare;

import java.util.ArrayList;

public class MsgRound1 {

    private long id;
    private ArrayList<CipherShare> ciperShares;

    public MsgRound1(long id, ArrayList<CipherShare> ciperShares) {
        this.id = id;
        this.ciperShares = ciperShares;
    }

    public ArrayList<CipherShare> getCiperShares() {
        return ciperShares;
    }

    public void setCiperShares(ArrayList<CipherShare> ciperShares) {
        this.ciperShares = ciperShares;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
