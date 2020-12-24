package edu.bjut.agg.messages;

import edu.bjut.agg.Shamir.SecretShare;
import it.unisa.dia.gas.jpbc.Element;

public class RepKeys {
	private long id;
	private SecretShare[] ci;
	private Element si;
	private int ti;
	public RepKeys(long id, SecretShare[] ci, Element si, int ti) {
		super();
		this.id = id;
		this.ci = ci;
		this.si = si;
		this.ti = ti;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public SecretShare[] getCi() {
		return ci;
	}
	public void setCi(SecretShare[] ci) {
		this.ci = ci;
	}
	public Element getSi() {
		return si;
	}
	public void setSi(Element si) {
		this.si = si;
	}
	public int getTi() {
		return ti;
	}
	public void setTi(int ti) {
		this.ti = ti;
	}

}
