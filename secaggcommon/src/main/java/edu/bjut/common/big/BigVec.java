package edu.bjut.common.big;

import java.math.BigInteger;
import java.util.Arrays;

import edu.bjut.common.util.PRG;

public class BigVec {

    private BigInteger[] bigs;
    private int size;

    public static BigVec Zero(int size) {
        BigVec v = new BigVec(size);
        for (int i = 0; i < size; ++i) {
            v.bigs[i] = BigInteger.ZERO;
        }
        return v;
    }

    public int size() {
        return size;
    }

    public static BigVec One(int size) {
        BigVec v = new BigVec(size);
        for (int i = 0; i < size; ++i) {
            v.bigs[i] = BigInteger.ONE;
        }
        return v;
    }

    public BigVec(int size) {
        this.size = size;
        this.bigs = new BigInteger[size];
    }

    public BigVec(BigInteger[] vals) {
        this.bigs = vals;
        this.size = vals.length;
    }
    
    public BigVec add(BigVec val) {
        var c = new BigVec(this.size);
        for (int i = 0; i < this.size; ++i) {
            c.bigs[i] = this.bigs[i].add(val.bigs[i]);
        }
        return c;
    }

    public BigVec subtract(BigVec val) {
        var c = new BigVec(this.size);
        for (int i = 0; i < this.size; ++i) {
            c.bigs[i] = this.bigs[i].subtract(val.bigs[i]);
        }
        return c;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.bigs);
    }

    @Override
    public boolean equals(Object anObject) {
        System.out.println("test");
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof BigVec) {
            BigVec aBigVec = (BigVec)anObject;
            if (aBigVec.size != this.size) return false;
            for (int i = 0; i < this.size; ++i) {
                if (!aBigVec.bigs[i].equals(bigs[i]))
                    return false;
            }
            return true;
        }
        return false;
    }

    public static BigVec genPRGBigVec(String seed, int bSize) {
        PRG prg = new PRG(seed);
        return new BigVec(prg.genBigs(bSize));
    }

	public BigVec multiply(BigInteger val) {
        BigVec v = BigVec.One(this.size);
        for (int i = 0; i < this.size; ++i) {
            v.bigs[i] = this.bigs[i].multiply(val);
        }
		return v;
	}
}