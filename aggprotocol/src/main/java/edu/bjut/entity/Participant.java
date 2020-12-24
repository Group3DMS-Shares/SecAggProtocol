package edu.bjut.entity;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

import edu.bjut.agg.Shamir.SecretShare;
import edu.bjut.agg.Shamir.SecretShareBigInteger;
import edu.bjut.agg.Shamir.Shamir;
import edu.bjut.agg.messages.ParamsECC;
import edu.bjut.agg.messages.RegBack;
import edu.bjut.agg.messages.RegBack2;
import edu.bjut.agg.messages.RegBack3;
import edu.bjut.agg.messages.RegMessage;
import edu.bjut.agg.messages.RegMessage2;
import edu.bjut.agg.messages.RegMessage3;
import edu.bjut.agg.messages.RepKeys;
import edu.bjut.agg.messages.RepMessage;
import edu.bjut.util.Utils;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class Participant {

	private long id;
	private Pairing pairing;

	private BigInteger di;
	private BigInteger ki;
	private Element qi;
	private Element ri;
	private Element zi;
	private long k;

	private BigInteger order;
	private Element g;
	private int count;

	ArrayList<Element> alQi = new ArrayList<Element>();
	ArrayList<SecretShareBigInteger> alKi = new ArrayList<SecretShareBigInteger>();

	public Participant(ParamsECC ps, int i) throws IOException {
		super();

		this.id = Utils.randomlong();
		this.pairing = ps.getPairing();
		this.g = ps.getGeneratorOfG1();

		this.order = pairing.getG1().getOrder();
		this.di = Utils.randomBig(order);

		this.ki = Utils.randomBig(order);

		this.ri = this.g.duplicate().pow(this.di);
		this.qi = this.g.duplicate().pow(this.ki);
	}

	/**
	 * A meter sends its identity and public key to aggregator for registration
	 */
	public RegMessage genRegMesssage() {
		RegMessage reg = new RegMessage(this.id, this.ri, this.qi);
		return reg;
	}

	/**
	 * A meter sends its identity and public key to aggregator for registration
	 */
	public RegMessage2 getRegBack(RegBack back) {
		alQi = back.getAlKeys();

		zi = back.getRi1().duplicate().mul(this.di);
		Element zi1 = back.getRiP1().duplicate().mul(this.di);

		Element xi = zi1.duplicate().sub(zi);
		RegMessage2 reg = new RegMessage2(xi);
		return reg;
	}

	/**
	 * A meter sends its identity and public key to aggregator for registration
	 */
	public RegMessage3 getRegBack2(RegBack2 back2) {
		Element tem = this.zi.duplicate().mul(BigInteger.valueOf(Params.PARTICIPANT_NUM));
		tem.mul(back2.getTi());
		String orgStr = tem.toString();

		this.k = Utils.hash2Big(orgStr, this.order).longValue();
		SecretShareBigInteger[] keys = genKi(Params.PARTICIPANT_NUM);

		RegMessage3 reg3 = new RegMessage3(this.id, keys);
		return reg3;
	}

	private SecretShareBigInteger[] genKi(int num) {
		SecureRandom random = new SecureRandom();
		SecretShareBigInteger[] shares = Shamir.split(ki, Params.RECOVER_K, Params.PARTICIPANT_NUM, order, random);
		return shares;
	}

	/**
	 * A meter sends its identity and public key to aggregator for registration
	 */
	public void getRegBack3(RegBack3 back3) {
		this.alKi = back3.getAlKeys();
	}

	public long getId() {
		return this.id;
	}

	/**
	 * A meter report multiple types of data to aggregator at a time
	 */
	public RepMessage genRepMessage() throws IOException {
		if (count++ > 2401)
			count = 1;

		BigInteger ci = getEncryptedWeights();

		Element temEle = Utils.hash2ElementG1(ci.toString() + id + count, this.pairing);
		Element si = temEle.duplicate().mul(this.di);

//		Element hr = Utils.hash2ElementG1(Integer.toString(count), pairing);
//		System.out.println("  ki " + hr.duplicate().mul(this.ki));
//		System.out.println();

		return new RepMessage(id, ci, si, count);
	}

	private BigInteger getEncryptedWeights() {
//		System.out.println(" count : " + count);
		BigInteger ci = BigInteger.valueOf(Utils.randomInt(100));
//		System.out.println("ci : " + ci);
		BigInteger pi = genPi();
//		System.out.println("pi : " + pi);
		ci = ci.add(pi);
		BigInteger ni = Utils.hash2Big(Long.toString(this.k + this.count), this.order);
		ci = ci.add(ni);
//		System.out.println("ni : " + ni );
		return ci;
	}

	// generates the keys
	public RepKeys genRepKeys(int fails[], int num) throws IOException {
		SecretShare[] ci = getShares(fails, num);

		Element temEle = Utils.hash2ElementG1(ci.toString() + id + count, this.pairing);
		Element si = temEle.duplicate().mul(this.di);
		return new RepKeys(id, ci, si, count);
	}

	private SecretShare[] getShares(int fails[], int num) {
		SecretShare[] ci = new SecretShare[num];
		int index = 0;
		Element hr = Utils.hash2ElementG1(Integer.toString(count), pairing);

		for (int i = 0; i < fails.length; i++) {
			if (fails[i] == 1) {
				Element temEle = hr.duplicate().mul(alKi.get(i).getShare());
				ci[index++] = new SecretShare(alKi.get(i).getNumber(), temEle);
			}
		}
		return ci;
	}

	private BigInteger genPi() {
		BigInteger pi = BigInteger.ZERO;
		int index = alQi.indexOf(this.qi);
		Element hr = Utils.hash2ElementG1(Integer.toString(count), pairing);

		for (int i = 0; i < index; i++) {
			Element tem = this.pairing.pairing(alQi.get(i), hr).duplicate().mul(this.ki);
			pi = pi.add(tem.toBigInteger());
		}
		for (int i = index + 1; i < alQi.size(); i++) {
			Element tem = this.pairing.pairing(alQi.get(i), hr).duplicate().mul(this.ki);
			pi = pi.subtract(tem.toBigInteger());
		}
		return pi;
	}

	public void getRepMessage(RepMessage rep) throws IOException {
		BigInteger ni = Utils.hash2Big(Long.toString(this.k + this.count), this.order);
		ni = ni.multiply(BigInteger.valueOf(Params.PARTICIPANT_NUM));
		System.out.println("data : " + rep.getCi().subtract(ni));
	}

	public void getRepMessageFails(RepMessage rep) throws IOException {
		BigInteger ni = Utils.hash2Big(Long.toString(this.k + this.count), this.order);
		ni = ni.multiply(BigInteger.valueOf(Params.PARTICIPANT_NUM - Params.PARTICIPANT_FAILS));
		System.out.println("data : " + (rep.getCi().subtract(ni)).mod(this.order));
	}

}
