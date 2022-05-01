package edu.bjut.aaia.messages;

import edu.bjut.common.big.BigVec;

public class MsgRound2 {
    private int id;
    private BigVec y;

    public MsgRound2(int id, BigVec y) {
        this.id = id;
        this.y = y;
    }

    public BigVec getY() {
        return y;
    }

    public void setY(BigVec y) {
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
