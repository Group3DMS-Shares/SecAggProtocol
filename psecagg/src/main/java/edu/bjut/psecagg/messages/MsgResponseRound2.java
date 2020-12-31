package edu.bjut.psecagg.messages;

import java.util.ArrayList;

public class MsgResponseRound2 {
    private ArrayList<Long> u3ids;

    public MsgResponseRound2(ArrayList<Long> u3ids) {
        this.u3ids = u3ids;
    }

    public ArrayList<Long> getU3ids() {
        return u3ids;
    }

    public void setU3ids(ArrayList<Long> u3ids) {
        this.u3ids = u3ids;
    }
}
