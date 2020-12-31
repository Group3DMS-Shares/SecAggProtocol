package edu.bjut.psecagg.messages;

import java.math.BigInteger;

public class MsgRound2 {
    private long id;
    private BigInteger y;

    public MsgRound2(long id, BigInteger y) {
        this.id = id;
        this.y = y;
    }

    public BigInteger getY() {
        return y;
    }

    public void setY(BigInteger y) {
        this.y = y;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
