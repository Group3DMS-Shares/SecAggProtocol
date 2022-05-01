package edu.bjut.aaia.messages;

import java.util.ArrayList;

public class MsgResponseRound2 {
    private ArrayList<Integer> u3ids;

    public MsgResponseRound2(ArrayList<Integer> u3ids) {
        this.u3ids = u3ids;
    }

    public ArrayList<Integer> getU3ids() {
        return u3ids;
    }

    public void setU3ids(ArrayList<Integer> u3ids) {
        this.u3ids = u3ids;
    }
}
