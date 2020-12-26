package edu.bjut.entity;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

import edu.bjut.Shamir.SecretShareBigInteger;
import edu.bjut.Shamir.Shamir;
import edu.bjut.util.Utils;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class User {
	private BigInteger delta; 
	private BigInteger rho;

	private BigInteger n_SK_n;
	private Element n_pK_n;

	private BigInteger p_SK_n;
	private Element p_pK_n;
	private long id;
	private ArrayList<MessagePubKeys> broadcastKeysList;

	private Pairing pairing;
	private Element g;
	private BigInteger q;

	private BigInteger beta;
	private ArrayList<MessagePNM> pmnList;
	
	public User() {
		this.id = Utils.randomlong();
		this.pmnList = new ArrayList<MessagePNM>();
	}

	public void setParamsECC(ParamsECC paramsECC) {
		this.pairing = paramsECC.getPairing();
		this.g = paramsECC.getGeneratorOfG1();
		this.q = this.pairing.getG1().getOrder();
	}

	public MessagePubKeys getPubKeys() {
		MessagePubKeys msgPubKeys = new MessagePubKeys(id, n_pK_n, p_pK_n);
		return msgPubKeys;
	}

	public SecretShareBigInteger[] genBetaShares() {
		this.beta = Utils.randomBig(q);
		SecureRandom random = new SecureRandom();
		SecretShareBigInteger[] shares = Shamir.split(beta, Params.RECOVER_K, Params.PARTICIPANT_NUM - 1, q, random);
		return shares;
	}

	public SecretShareBigInteger[] genN_SK_nShares() {
		SecureRandom random = new SecureRandom();
		SecretShareBigInteger[] shares = Shamir.split(this.n_SK_n, Params.RECOVER_K, Params.PARTICIPANT_NUM - 1, q, random);
		return shares;
	}

	private void genKA_Agree(Element p_Pk_m) {
		Element snm = p_Pk_m.duplicate().mul(this.p_SK_n);
		String snm_string = snm.toString();	
	}

	private BigInteger genKA_AgreeMaskedInput(Element n_Pk_m) {
		Element snm = n_Pk_m.duplicate().mul(this.n_SK_n);
		return Utils.hash2Big(snm.toString(), this.q);
	}

	private BigInteger genMaskedInputI() {
		return this.beta;
	}

	private BigInteger genMaskedInputII() {
		BigInteger pi = BigInteger.ZERO;
		MessagePubKeys messagePubKeys = new MessagePubKeys(this.id, this.n_pK_n, this.p_pK_n);
		int index = broadcastKeysList.indexOf(messagePubKeys);
		System.out.println("genStart");
		for (MessagePubKeys messagePubKeys2: broadcastKeysList) {
			System.out.println(messagePubKeys2.getIdm());
		}
		System.out.println("=================");
		for (int i = 0; i < broadcastKeysList.size(); ++i) {
			if (this.broadcastKeysList.get(i).getIdm() == this.id) index = i;
		}
		System.out.println(this.id);
		System.out.println(index);

		for (int i = 0; i < index; i++) {
			BigInteger tem = genKA_AgreeMaskedInput(broadcastKeysList.get(i).getN_pK_n());
			pi = pi.add(tem);
			System.out.println("..........................");
			System.out.println(tem);
		}
		for (int i = index + 1; i < broadcastKeysList.size(); i++) {
			BigInteger tem = genKA_AgreeMaskedInput(broadcastKeysList.get(i).getN_pK_n());
			pi = pi.subtract(tem);
			System.out.println("..........................");
			System.out.println(tem);
		}
		System.out.println("=================");
		return pi;
	}
	 
	public BigInteger genEncGradient() {
		BigInteger x_n = BigInteger.ONE;
		BigInteger x_n_prim = x_n.add(genMaskedInputII());
		return x_n_prim;
	}

	public ArrayList<MessagePNM> genMsgPNMs() {
		SecretShareBigInteger[] shareBeta = genBetaShares();
		SecretShareBigInteger[] shareN_Sk_n_m = genN_SK_nShares();
		int index = 0;
		ArrayList<MessagePNM> messagePNMLists= new ArrayList<MessagePNM>();

		System.out.println(shareBeta.length);
		for (MessagePubKeys messagePubKeys: broadcastKeysList) {
			if (messagePubKeys.getIdm() == this.id) {
				messagePNMLists.add(null);
				continue;
			} 
			MessagePNM messagePNM = new MessagePNM(this.id, messagePubKeys.getIdm(),
			 							shareN_Sk_n_m[index], shareBeta[index]);
			messagePNMLists.add(messagePNM);
			index++;
		}
		return messagePNMLists;
	}

	public void appendMessagePMN(MessagePNM messagePMN) {
		this.pmnList.add(messagePMN);
	}

	public void setKeys(MessageKeys msgKeys) {
		this.delta = msgKeys.getDelta();
		this.rho = msgKeys.getRho();
		this.n_SK_n = msgKeys.getN_SK_n();
		this.n_pK_n = msgKeys.getP_pK_n();
		this.p_SK_n = msgKeys.getP_SK_n();
		this.p_pK_n = msgKeys.getP_pK_n();
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

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ArrayList<MessagePubKeys> getBroadcastKeysList() {
		return broadcastKeysList;
	}

	public void setBroadcastKeysList(ArrayList<MessagePubKeys> broadcastKeysList) {
		this.broadcastKeysList = broadcastKeysList;
	}
    
}

