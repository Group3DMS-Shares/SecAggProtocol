package edu.bjut.common.shamir;
import java.math.BigInteger;

import it.unisa.dia.gas.jpbc.Element;

public class SecretShare
{
    public SecretShare(final BigInteger number, final Element share)
    {
        this.number = number;
        this.share = share;
    }

    public BigInteger getNumber()
    {
        return number;
    }

    public Element getShare()
    {
        return share;
    }

    @Override
    public String toString()
    {
        return "SecretShare [num=" + number + ", share=" + share + "]";
    }

    private final BigInteger number;
    private final Element share;
}