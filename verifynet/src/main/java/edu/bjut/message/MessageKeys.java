package edu.bjut.message;

import java.math.BigInteger;

import it.unisa.dia.gas.jpbc.Element;

public class MessageKeys {
	private BigInteger delta; 
	private BigInteger rho;

	private BigInteger n_sK_n;
	private Element n_pK_n;

	private BigInteger p_sK_n;
	private Element p_pK_n;

	public MessageKeys(BigInteger delta, BigInteger rho, BigInteger n_sK_n, Element n_pK_n, BigInteger p_sK_n,
			Element p_pK_n) {
		this.delta = delta;
		this.rho = rho;
		this.n_sK_n = n_sK_n;
		this.n_pK_n = n_pK_n;
		this.p_sK_n = p_sK_n;
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

	public BigInteger getN_sK_n() {
		return n_sK_n;
	}

	public void setN_sK_n(BigInteger n_sK_n) {
		this.n_sK_n = n_sK_n;
	}

	public Element getN_pK_n() {
		return n_pK_n;
	}

	public void setN_pK_n(Element n_pK_n) {
		this.n_pK_n = n_pK_n;
	}

	public BigInteger getP_sK_n() {
		return p_sK_n;
	}

	public void setP_sK_n(BigInteger p_sK_n) {
		this.p_sK_n = p_sK_n;
	}

	public Element getP_pK_n() {
		return p_pK_n;
	}

	public void setP_pK_n(Element p_pK_n) {
		this.p_pK_n = p_pK_n;
	}
}
