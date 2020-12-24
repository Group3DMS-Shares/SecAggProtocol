package edu.bjut.entity;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

import edu.bjut.agg.Shamir.SecretShare;
import edu.bjut.agg.Shamir.SecretShareBigInteger;
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
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class ParameterServer {

	private long id;
	private Pairing pairing;
	private Element g;
	private BigInteger dj;
	private Element rj;
	private BigInteger order;

	ArrayList<Long> alId = new ArrayList<Long>();
	ArrayList<Element> alKeys = new ArrayList<Element>();
	ArrayList<Element> alQi = new ArrayList<Element>();
	ArrayList<Element> alXi = new ArrayList<Element>();

	private ArrayList<SecretShareBigInteger[]> alki = new ArrayList<SecretShareBigInteger[]>();
	private ArrayList<RepMessage> alRep = new ArrayList<RepMessage>();
	private ArrayList<RepKeys> alRepKeys = new ArrayList<RepKeys>();

	public ParameterServer() throws IOException {

		super();
		this.pairing = PairingFactory.getPairing("aggVote1.properties");

		this.id = Utils.randomlong();
		this.order = pairing.getG1().getOrder();
		this.g = this.pairing.getG1().newRandomElement().getImmutable();

		this.dj = Utils.randomBig(order);
	}

	public ParamsECC getParamsECC() {
		ParamsECC ps = new ParamsECC(this.pairing, this.g);
		return ps;
	}

	// the first round of registration messages
	public void getRegMessage(RegMessage reg) {
		alId.add(reg.getId());
		alKeys.add(reg.getKey());
		alQi.add(reg.getQi());
	}

	// the first round of registration messages
	public RegBack genRegBack(int index) {
		int pre = (index - 1 + alKeys.size()) % alKeys.size();
		int rear = (index + 1) % alKeys.size();

		RegBack back = new RegBack(alQi, alKeys.get(pre), alKeys.get(rear));
		return back;
	}

	// the second second second round of registration messages
	public void getRegMessage2(RegMessage2 reg2) {
		alXi.add(reg2.getXi());
	}

	// the second second second round of registration messages
	public RegBack2 genRegBack2(int index) {
		int n = alXi.size();
		int c = n - 1;
		Element ti = pairing.getG1().newOneElement();
		for (int i = 0; i < n - 1; i++) {
			int rear = (index + i) % alKeys.size();

			Element tem = alXi.get(rear).duplicate().mul(BigInteger.valueOf(c--));
			ti = ti.duplicate().mul(tem);
		}

		RegBack2 back = new RegBack2(ti);
		return back;
	}

	// the third third third round of registration messages
	public void getRegMessage3(RegMessage3 reg2) {
		this.alki.add(reg2.getKeys());
	}

	// the third third third round of registration messages
	public RegBack3 genRegBack3(int i) {
		ArrayList<SecretShareBigInteger> alkiBack = new ArrayList<SecretShareBigInteger>();

		Iterator<SecretShareBigInteger[]> itKi = alki.iterator();
		while (itKi.hasNext()) {
			SecretShareBigInteger tem = itKi.next()[i];
			alkiBack.add(tem);
		}
		RegBack3 back = new RegBack3(alkiBack);
		return back;
	}

	public RepMessage getRepMessage(RepMessage rep) throws IOException {
		alRep.add(rep);
		if (alRep.size() < Params.PARTICIPANT_NUM)
			return null;

		if (false == checkingIncomeMessage()) {
			System.out.println("check failed at the agg side");
			return null;
		}
		return genRepMessage(sumUpReportingData(), rep.getTi());
	}

	public RepMessage getRepKeys(RepKeys rep) throws IOException {
		alRepKeys.add(rep);
		if (alRepKeys.size() < Params.RECOVER_K) {
			return null;
		}

		if (false == checkingRepKeys()) {
			System.out.println("check failed at the agg side");
			return null;
		}
		return genRepMessage(sumUpFailsData(), rep.getTi());
	}

	/**
	 * A meter report multiple types of data to aggregator at a time
	 */
	public RepMessage genRepMessage(BigInteger data, int count) throws IOException {
		Element temEle = Utils.hash2ElementG1(data.toString() + id + count, this.pairing);
		Element si = temEle.duplicate().mul(this.dj);
		return new RepMessage(id, data, si, count);
	}

	private BigInteger sumUpReportingData() throws IOException {

		Iterator<RepMessage> itRep = alRep.iterator();
		BigInteger ci = BigInteger.ZERO;

		while (itRep.hasNext()) {
			ci = ci.add(itRep.next().getCi());
		}

		clearReportMessage();
		return ci;
	}

	private BigInteger sumUpFailsData() throws IOException {

		Iterator<RepMessage> itRep = alRep.iterator();
		BigInteger ci = BigInteger.ZERO;
		while (itRep.hasNext()) {
			ci = ci.add(itRep.next().getCi()).mod(this.order);
		}

		Iterator<RepKeys> itRepKeys = alRepKeys.iterator();
		SecretShare[][] keys = new SecretShare[alRepKeys.size()][Params.PARTICIPANT_FAILS];

		int index = 0;
		while (itRepKeys.hasNext()) {
			keys[index] = itRepKeys.next().getCi();
			index++;
		}
		SecretShare[] sharesToViewSecret = new SecretShare[Params.RECOVER_K];

		int[] pos = getPoss();
		
		for (int i = 0; i < keys.length; i++) {
			for (int j = 0; j < keys[0].length; j++) {
				System.out.println(keys[i][j]);
			}
		}
		
		for (int i = 0; i < Params.PARTICIPANT_FAILS; i++) {
			for (int j = 0; j < Params.RECOVER_K; j++) {

				BigInteger aBigInteger = keys[j][i].getNumber().multiply(BigInteger.valueOf(11));
				Element aElement = keys[j][i].getShare();

				sharesToViewSecret[j] = new SecretShare(aBigInteger, aElement);
			}
			Element tem = combine2(sharesToViewSecret);

			System.out.println(" index : " + pos[i]);
			BigInteger pi = genPi(tem, pos[i]);
			ci = ci.add(pi);
		}

		clearReportMessage();
		return ci;
	}

	private int[] getPoss() {
		int[] pos = new int[Params.PARTICIPANT_FAILS];
		int index = 0;
		for (int i = 0; i < Params.fails.length; i++) {
			if (Params.fails[i] == 1) {
				pos[index++] = i;
			}
		}
		return pos;
	}

	private BigInteger genPi(Element tems, int index) {
		BigInteger pi = BigInteger.ZERO;

		for (int i = 0; i < index; i++) {
			Element tem = this.pairing.pairing(alQi.get(i), tems);
			pi = pi.add(tem.toBigInteger());
		}

		for (int i = index + 1; i < alQi.size(); i++) {
			Element tem = this.pairing.pairing(alQi.get(i), tems);
			pi = pi.subtract(tem.toBigInteger());
		}
		System.out.println("pi test : " + pi.toString());
		return pi;
	}

	public Element combine2(final SecretShare[] shares) {
		Element accum = this.pairing.getG1().newOneElement();

		for (int formula = 0; formula < shares.length; formula++) {
			BigInteger numerator = BigInteger.ONE;
			BigInteger denominator = BigInteger.ONE;

			for (int count = 0; count < shares.length; count++) {
				if (formula == count)
					continue; // If not the same value

				BigInteger startposition = shares[formula].getNumber();
				BigInteger nextposition = shares[count].getNumber();

				numerator = numerator.multiply((nextposition).negate()).mod(this.order);
				denominator = denominator.multiply(startposition.subtract(nextposition)).mod(this.order);
			}

			Element value = shares[formula].getShare();
			BigInteger tmp = (numerator).multiply(modInverse(denominator, this.order));

			Element temEle = value.duplicate().mul(tmp);
			accum = accum.duplicate().add(temEle);
		}
//		System.out.println();
		System.out.println("sec: " + accum);
		return accum;
	}

	private static BigInteger[] gcdD(BigInteger a, BigInteger b) {
		if (b.compareTo(BigInteger.ZERO) == 0)
			return new BigInteger[] { a, BigInteger.ONE, BigInteger.ZERO };
		else {
			BigInteger n = a.divide(b);
			BigInteger c = a.mod(b);
			BigInteger[] r = gcdD(b, c);
			return new BigInteger[] { r[0], r[2], r[1].subtract(r[2].multiply(n)) };
		}
	}

	private static BigInteger modInverse(BigInteger k, BigInteger prime) {
		k = k.mod(prime);
		BigInteger r = (k.compareTo(BigInteger.ZERO) == -1) ? (gcdD(prime, k.negate())[2]).negate() : gcdD(prime, k)[2];
		return prime.add(r).mod(prime);
	}

	private BigInteger genPi(Element[][] keys, int index, int pos) {
		BigInteger pi = BigInteger.ZERO;

		for (int i = 0; i < index; i++) {
			if (null == keys[pos][i])
				continue;
//			System.out.println(" tem " + keys[pos][i]);
			BigInteger tem = (keys[pos][i]).toBigInteger();
//			System.out.println(" tem " + tem);
			pi = pi.add(tem);
		}
		for (int i = index + 1; i < keys[pos].length; i++) {
			if (null == keys[pos][i])
				continue;
//			System.out.println(" tem " + keys[pos][i]);
			BigInteger tem = (keys[pos][i]).toBigInteger();
//			System.out.println(" tem " + tem);
			pi = pi.subtract(tem);
		}
		System.out.println("pi test : " + pi.toString());
		return pi;
	}

	private boolean checkingRepKeys() throws IOException {

		ArrayList<BigInteger> alFai = prepareFai();

		Element left = PrepareLeftRepKeys(alFai);
		Element right = PrepareRightRepKeys(alFai);

		if (!left.equals(right)) {
			System.out.println("left ::: " + left);
			System.out.println("right::: " + right);
			System.out.println("RepKeys RepKeys RepKeys not equal to left failed!");
		} else {
//			System.out.println("recovering keys, please wait!!!");
		}
		return true;
	}

	private Element PrepareLeftRepKeys(ArrayList<BigInteger> alFai) {

		Iterator<RepKeys> itRep = alRepKeys.iterator();
		Iterator<BigInteger> itFai = alFai.iterator();

		if (!itRep.hasNext()) {
			return null;
		}

		Element temResult = pairing.getG1().newZeroElement();
		while (itRep.hasNext()) {
			temResult.add(itRep.next().getSi().duplicate().pow(itFai.next()));
		}

		Element result = pairing.pairing(temResult, this.g);
		return result;
	}

	private Element PrepareRightRepKeys(ArrayList<BigInteger> alFai) {

		Iterator<RepKeys> itRep = alRepKeys.iterator();
		Iterator<BigInteger> itFai = alFai.iterator();

		if (!itRep.hasNext()) {
			return null;
		}

		Element result = pairing.getGT().newOneElement();

		Element temHash;
		Element temRi;
		Element temPairing;

		while (itRep.hasNext()) {

			RepKeys rep = itRep.next();
			temHash = Utils.hash2ElementG1(rep.getCi().toString() + rep.getId() + rep.getTi(), this.pairing);

			temRi = getPublicKeyById(rep.getId());
			temPairing = pairing.pairing(temHash, temRi.duplicate().pow(itFai.next()));
			result.mul(temPairing);
		}
		return result;
	}

	private boolean checkingIncomeMessage() throws IOException {

		ArrayList<BigInteger> alFai = prepareFai();

		Element left = PrepareLeft(alFai);
		Element right = PrepareRight(alFai);

		if (!left.equals(right)) {
			System.out.println("left ::: " + left);
			System.out.println("right::: " + right);
			System.out.println("rep rep rep not equal to left failed!");
//			System.exit(1);
		} else {
			System.out.println("preparing data, please wait!!!");
		}
		return true;
	}

	private ArrayList<BigInteger> prepareFai() {
		ArrayList<BigInteger> alFai = new ArrayList<BigInteger>();
		for (int i = 0; i < Params.PARTICIPANT_NUM; i++) {
			alFai.add(Utils.randomFai());
		}
		return alFai;
	}

	private Element PrepareLeft(ArrayList<BigInteger> alFai) {

		Iterator<RepMessage> itRep = alRep.iterator();
		Iterator<BigInteger> itFai = alFai.iterator();

		if (!itRep.hasNext()) {
			return null;
		}

		Element temResult = pairing.getG1().newZeroElement();
		while (itRep.hasNext()) {
			temResult.add(itRep.next().getSi().duplicate().pow(itFai.next()));
		}

		Element result = pairing.pairing(temResult, this.g);
		return result;
	}

	private Element PrepareRight(ArrayList<BigInteger> alFai) {

		Iterator<RepMessage> itRep = alRep.iterator();
		Iterator<BigInteger> itFai = alFai.iterator();

		if (!itRep.hasNext()) {
			return null;
		}

		Element result = pairing.getGT().newOneElement();

		Element temHash;
		Element temRi;
		Element temPairing;

		while (itFai.hasNext()) {

			RepMessage rep = itRep.next();
			temHash = Utils.hash2ElementG1(rep.getCi().toString() + rep.getId() + rep.getTi(), this.pairing);

			temRi = getPublicKeyById(rep.getId());
			temPairing = pairing.pairing(temHash, temRi.duplicate().pow(itFai.next()));
			result.mul(temPairing);
		}
		return result;
	}

	private Element getPublicKeyById(long id) {
		int index = alId.indexOf(id);
		return alKeys.get(index);
	}

	private void clearReportMessage() throws IOException {
		alRep.clear();
		alki.clear();
		alRepKeys.clear();
	}

	public void clear() throws IOException {
		alId.clear();
		alKeys.clear();
		alXi.clear();
		alki.clear();
		alQi.clear();
	}

	public Element assignMeterKeys(long id2, int i) {
		// TODO Auto-generated method stub
		return null;
	}

}
