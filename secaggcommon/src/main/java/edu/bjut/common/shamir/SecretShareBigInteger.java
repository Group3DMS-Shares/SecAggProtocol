package edu.bjut.common.shamir;
import java.math.BigInteger;

public class SecretShareBigInteger
{
    public SecretShareBigInteger(final BigInteger number, final BigInteger share)
    {
        this.number = number;
        this.share = share;
    }

    public BigInteger getNumber()
    {
        return number;
    }

    public BigInteger getShare()
    {
        return share;
    }

    @Override
    public String toString()
    {
        return "SecretShare [num=" + number + ", share=" + share + "]";
    }

    private final BigInteger number;
    private final BigInteger share;
}