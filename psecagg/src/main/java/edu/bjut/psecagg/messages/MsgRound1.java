package edu.bjut.psecagg.messages;

import edu.bjut.psecagg.entity.UVShares;

import java.util.ArrayList;

public class MsgRound1 {

    private long id;
    private ArrayList<UVShares> uvSharesList;

    public MsgRound1(long id, ArrayList<UVShares> uvSharesList) {
        this.id = id;
        this.uvSharesList = uvSharesList;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ArrayList<UVShares> getUvSharesList() {
        return uvSharesList;
    }

    public void setUvSharesList(ArrayList<UVShares> uvSharesList) {
        this.uvSharesList = uvSharesList;
    }


}
