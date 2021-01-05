package edu.bjut.psecagg.messages;

import edu.bjut.psecagg.entity.CipherShare;
import edu.bjut.psecagg.entity.UVShare;

import java.util.ArrayList;

public class MsgRound1 {

    private long id;
    private ArrayList<UVShare> uvShareList;
    private ArrayList<CipherShare> ciperShares;

    public MsgRound1(long id, ArrayList<UVShare> uvShareList, ArrayList<CipherShare> ciperShares) {
        this.id = id;
        this.uvShareList = uvShareList;
        this.ciperShares = ciperShares;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ArrayList<UVShare> getUvSharesList() {
        return uvShareList;
    }

    public void setUvSharesList(ArrayList<UVShare> uvShareList) {
        this.uvShareList = uvShareList;
    }


}
