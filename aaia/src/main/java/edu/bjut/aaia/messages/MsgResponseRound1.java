package edu.bjut.aaia.messages;

import java.util.ArrayList;

public class MsgResponseRound1 {
    public ArrayList<MsgRound1> getMsgRound1s() {
        return msgRound1s;
    }

    private ArrayList<MsgRound1> msgRound1s;
    public MsgResponseRound1(ArrayList<MsgRound1> msgRound1s) {
        this.msgRound1s = msgRound1s;
    }
}
