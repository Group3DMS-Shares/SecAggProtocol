package edu.bjut.psecagg.entity;

public class CipherShare {
    private byte[] uId;
    private byte[] vId;
    private byte[] buNumer;
    private byte[] buShare;
    private byte[] suNumer;
    private byte[] suShare;

    public byte[] getSuNumer() {
        return suNumer;
    }

    public void setSuNumer(byte[] suNumer) {
        this.suNumer = suNumer;
    }

    public byte[] getSuShare() {
        return suShare;
    }

    public void setSuShare(byte[] suShare) {
        this.suShare = suShare;
    }

    public byte[] getuId() {
        return uId;
    }

    public void setuId(byte[] uId) {
        this.uId = uId;
    }

    public byte[] getvId() {
        return vId;
    }

    public void setvId(byte[] vId) {
        this.vId = vId;
    }

    public byte[] getBuNumer() {
        return buNumer;
    }

    public void setBuNumer(byte[] buNumer) {
        this.buNumer = buNumer;
    }

    public byte[] getBuShare() {
        return buShare;
    }

    public void setBuShare(byte[] buShare) {
        this.buShare = buShare;
    }

    public CipherShare(byte[] uId, byte[] vId, byte[] buNumer, byte[] buShare, byte[] suNumer, byte[] suShare) {
        this.uId = uId;
        this.vId = vId;
        this.buNumer = buNumer;
        this.buShare = buShare;
        this.suNumer = suNumer;
        this.suShare = suShare;
    }


}
