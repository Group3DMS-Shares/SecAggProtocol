package edu.bjut.psecagg.messages;

import java.util.ArrayList;

public class MsgResponseRound0 {
    private ArrayList<MsgRound0> pubKeys;

    public ArrayList<MsgRound0> getPubKeys() {
        return pubKeys;
    }

    public void setPubKeys(ArrayList<MsgRound0> pubKeys) {
        this.pubKeys = pubKeys;
    }

    public MsgResponseRound0(ArrayList<MsgRound0> pubKeys) {
        this.pubKeys = pubKeys;
    }
}
