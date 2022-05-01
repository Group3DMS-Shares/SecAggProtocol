package edu.bjut.verifynet.messages;

import edu.bjut.common.big.BigVec;

public class MsgRound2 {
    private long id;
    private BigVec y;

    public MsgRound2(long id, BigVec y) {
        this.id = id;
        this.y = y;
    }

    public BigVec getY() {
        return y;
    }

    public void setY(BigVec y) {
        this.y = y;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
