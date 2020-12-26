package edu.bjut.entity;

import java.math.BigInteger;

import it.unisa.dia.gas.jpbc.Element;

public class MessageKeys {
	private BigInteger delta; 
	private BigInteger rho;
	private BigInteger n_SK_n;
	private BigInteger p_SK_n;
	private Element n_pK_n;
	private Element p_pK_n;

	MessageKeys(BigInteger delta, BigInteger rho, BigInteger n_SK_n,
				BigInteger p_SK_n, Element n_pK_n, Element p_pK_n) {
					this.delta = delta;
					this.rho = rho;
					this.n_SK_n = n_SK_n;
					this.p_SK_n = p_SK_n;
					this.n_pK_n = n_pK_n;
					this.p_pK_n = p_pK_n;
	}

	public BigInteger getDelta() {
		return delta;
	}

	public void setDelta(BigInteger delta) {
		this.delta = delta;
	}

	public BigInteger getRho() {
		return rho;
	}

	public void setRho(BigInteger rho) {
		this.rho = rho;
	}

	public BigInteger getN_SK_n() {
		return n_SK_n;
	}

	public void setN_SK_n(BigInteger n_SK_n) {
		this.n_SK_n = n_SK_n;
	}

	public BigInteger getP_SK_n() {
		return p_SK_n;
	}

	public void setP_SK_n(BigInteger p_SK_n) {
		this.p_SK_n = p_SK_n;
	}

	public Element getN_pK_n() {
		return n_pK_n;
	}

	public void setN_pK_n(Element n_pK_n) {
		this.n_pK_n = n_pK_n;
	}

	public Element getP_pK_n() {
		return p_pK_n;
	}

	public void setP_pK_n(Element p_pK_n) {
		this.p_pK_n = p_pK_n;
	}
    
}
