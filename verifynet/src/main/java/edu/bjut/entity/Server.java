package edu.bjut.entity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.bjut.Shamir.SecretShareBigInteger;
import edu.bjut.Shamir.Shamir;
import edu.bjut.message.MessageBetaShare;
import edu.bjut.message.MessageDroupoutShare;
import edu.bjut.message.MessagePNM;
import edu.bjut.message.MessagePubKeys;
import edu.bjut.message.MessageSigma;
import edu.bjut.util.Utils;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class Server {
	private ArrayList<MessagePubKeys> msgPubkeysList;

	private ArrayList<ArrayList<MessagePNM>> messagePNMsInServer;
	private ArrayList<MessageSigma> messageSigmasInServer;
	private ArrayList<Long> receiveSigmaIds;

	private Map<Long, ArrayList<SecretShareBigInteger>> recoverBeta;
	private Map<Long, ArrayList<SecretShareBigInteger>> recovernSk;

	private ParamsECC paramsECC;
	private Pairing pairing;
	private BigInteger q;
	private int u1Count;

	public Server() {
		this.msgPubkeysList = new ArrayList<MessagePubKeys>();
		this.messagePNMsInServer = new ArrayList<ArrayList<MessagePNM>>();
		this.messageSigmasInServer = new ArrayList<MessageSigma>();
		this.receiveSigmaIds = new ArrayList<Long>();
		this.recoverBeta = new HashMap<Long, ArrayList<SecretShareBigInteger>>();
		this.recovernSk = new HashMap<Long, ArrayList<SecretShareBigInteger>>();
		this.u1Count = 0;
	}

	public void broadcastTo(ArrayList<User> users) {
		for (User u : users) {
			u.setBroadcastPubKeysList(this.msgPubkeysList);
		}
	}

	public void appendMessagePubkey(MessagePubKeys messagePubKeys) {
		this.msgPubkeysList.add(messagePubKeys);
		++u1Count;
	}

	public void appendMessagePNMs(ArrayList<MessagePNM> messagePNMs) {
		this.messagePNMsInServer.add(messagePNMs);
	}

	public void appendMessageSigma(MessageSigma messageSigma) {
		this.messageSigmasInServer.add(messageSigma);
		this.receiveSigmaIds.add(messageSigma.getId());
	}

	public void recoverSecret(int index) {
		ArrayList<MessagePNM> messagePNMs = messagePNMsInServer.get(0);
		SecretShareBigInteger[] shares = new SecretShareBigInteger[messagePNMs.size() - 1];
		int count = 0;
		for (MessagePNM messagePNM : messagePNMs) {
			if (null == messagePNM)
				continue;
			shares[count++] = messagePNM.getBeta_n_m();
		}

		BigInteger result = Shamir.combine(shares, q);
		System.out.println(result);
	}

	public ParamsECC getParamsECC() {
		return paramsECC;
	}

	public void setParamsECC(ParamsECC paramsECC) {
		this.paramsECC = paramsECC;
		this.pairing = paramsECC.getPairing();
		this.q = this.pairing.getG1().getOrder();
	}

	public void broadcastToPMN(ArrayList<User> users) {
		for (int i = 0; i < Params.PARTICIPANT_NUM; ++i) {
			for (int j = 0; j < Params.PARTICIPANT_NUM; ++j) {
				if (i == j) {
					users.get(i).appendMessagePMN(null);
				} else {
					users.get(i).appendMessagePMN(this.messagePNMsInServer.get(j).get(i));
				}
			}
		}
	}

	public void aggregateResult(ArrayList<User> users) {
		BigInteger result = BigInteger.ZERO;
		for (User u : users) {
			result = result.add(u.genEncGradient());
		}
		System.out.println("Aggregate: " + result);
	}

	public void broadcastToIds(ArrayList<User> users) {
		for (User u : users) {
			u.getU3ids().addAll(receiveSigmaIds);
		}
	}

	public void receiveMsgAggBeta(ArrayList<MessageBetaShare> sendBetaShare) {
		for (MessageBetaShare mBetaShare : sendBetaShare) {
			long id = mBetaShare.getToId();
			if (recoverBeta.get(id) == null) {
				recoverBeta.put(id, new ArrayList<>());
			}
			recoverBeta.get(id).add(mBetaShare.getBetaNtoM());
		}
	}

	public void receiveMsgAggDroupout(ArrayList<MessageDroupoutShare> sendDropout) {
		for (MessageDroupoutShare mDroupoutShare : sendDropout) {
			long id = mDroupoutShare.getId();
			if (recovernSk.get(id) == null)
				recovernSk.put(id, new ArrayList<>());
			recovernSk.get(id).add(mDroupoutShare.getNsKm_NtoM());
		}
	}

	private BigInteger recoverSnm() {
		System.out.println("droupout number: " + recovernSk.size());
		for (Long id : recovernSk.keySet()) {
			System.out.println("droupout id:" + id);
		}
		// recover Nsk
		Map<Long, BigInteger> droupoutNsk = new HashMap<>();
		for (Entry<Long, ArrayList<SecretShareBigInteger>> e : this.recovernSk.entrySet()) {
			ArrayList<SecretShareBigInteger> nSkmShares = e.getValue();
			if (null != nSkmShares) {
				SecretShareBigInteger[] shares = new SecretShareBigInteger[nSkmShares.size()];
				System.out.println("nSkn recover");
				BigInteger nSk = Shamir.combine(nSkmShares.toArray(shares), this.q);
				droupoutNsk.put(e.getKey(), nSk);
			}
		}
		BigInteger omegaSnm = BigInteger.ZERO;
		for (Entry<Long, BigInteger> m: droupoutNsk.entrySet()) {
			for (Long n: receiveSigmaIds) {
				if (m.getKey() == n) continue;
				// id process
				Element nPk = msgPubkeysList.get(n.intValue()).getN_pK_n().duplicate();
				BigInteger mSk = m.getValue();
				Element snm = nPk.mul(mSk);
				BigInteger s = Utils.hash2Big(snm.toString(), this.q);
				System.out.println(n + " to " + m.getKey() + ", KA agree: " + snm);
				if (m.getKey() < n) {
					omegaSnm = omegaSnm.subtract(s);
				} else {
					omegaSnm = omegaSnm.add(s);
				}
			}
		}
		System.out.println("omega snm: " + omegaSnm);
		return omegaSnm;
	}

	private BigInteger recoverBeta() {
		BigInteger beta = BigInteger.ZERO;
		for (Long l: this.recoverBeta.keySet()) {
			ArrayList<SecretShareBigInteger> betaShares = this.recoverBeta.get(l);
			if (null != betaShares) {
				SecretShareBigInteger[] shares = new SecretShareBigInteger[betaShares.size()];
				System.out.println("beta recover");
				BigInteger Beta = Shamir.combine(betaShares.toArray(shares), this.q);
				beta = beta.add(Beta);
			}
		}
		return beta;
	}

	public BigInteger calculateOmeagXn() {
		BigInteger omegaXn = BigInteger.ZERO;
		for (MessageSigma mSigma: messageSigmasInServer) {
			omegaXn = omegaXn.add(mSigma.getX_n_hat());
		}
		BigInteger omegabeta = recoverBeta();
		omegaXn = omegaXn.subtract(omegabeta);
		omegaXn = omegaXn.add(recoverSnm());
		return omegaXn;
	}

	public void broadcastToAggResultAndProof(ArrayList<User> users) {
		System.out.println("Aggregation Result: " + calculateOmeagXn());
	}

	public boolean checkU1Count(int rECOVER_K) {
		return u1Count >= rECOVER_K;
	}
    
}
