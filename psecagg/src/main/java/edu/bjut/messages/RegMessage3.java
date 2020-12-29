package edu.bjut.messages;

import edu.bjut.Shamir.SecretShareBigInteger;

public class RegMessage3 {
    
    private long id;
    private SecretShareBigInteger [] keys;
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public SecretShareBigInteger[] getKeys() {
        return keys;
    }
    public void setKeys(SecretShareBigInteger[] keys) {
        this.keys = keys;
    }
    public RegMessage3(long id, SecretShareBigInteger[] keys) {
        super();
        this.id = id;
        this.keys = keys;
    }
    
}
