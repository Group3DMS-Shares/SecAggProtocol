package edu.bjut.verifynet.messages;

import java.util.ArrayList;

public class MsgResponseRound3 {
    public ArrayList<MsgRound3> getSigmas() {
        return sigmas;
    }

    public void setSigmas(ArrayList<MsgRound3> sigmas) {
        this.sigmas = sigmas;
    }

    public MsgResponseRound3(ArrayList<MsgRound3> sigmas) {
        this.sigmas = sigmas;
    }

    private ArrayList<MsgRound3> sigmas;
}
