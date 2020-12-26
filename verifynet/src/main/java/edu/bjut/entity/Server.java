package edu.bjut.entity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.bjut.Shamir.SecretShareBigInteger;
import edu.bjut.Shamir.Shamir;
import it.unisa.dia.gas.jpbc.Pairing;

public class Server {
	// TODO
	private ArrayList<MessagePubKeys> msgPubkeysList;

	private	ArrayList<ArrayList<MessagePNM>> messagePNMsInServer;
	private	ArrayList<MessageSigma> messageSigmasInServer;
	private ArrayList<Long> receiveSigmaIds;

	private Map<Long, ArrayList<SecretShareBigInteger>> recoverBeta;
	private Map<Long, ArrayList<SecretShareBigInteger>> recoverSnm;

	private ParamsECC paramsECC;
	private Pairing pairing;
	private BigInteger q;

	public Server() {
		this.msgPubkeysList = new ArrayList<MessagePubKeys>();
		this.messagePNMsInServer = new ArrayList<ArrayList<MessagePNM>>();
		this.messageSigmasInServer = new ArrayList<MessageSigma>();
		this.receiveSigmaIds = new ArrayList<Long>();
		this.recoverBeta = new HashMap<Long, ArrayList<SecretShareBigInteger>>();
		this.recoverSnm = new HashMap<Long, ArrayList<SecretShareBigInteger>>();
	}

	public void broadcastTo(ArrayList<User> users) {
		for (User u: users) {
			u.setBroadcastKeysList(this.msgPubkeysList);
		}
	}
	 
	public void  appendMessagePubkey(MessagePubKeys messagePubKeys) {
		this.msgPubkeysList.add(messagePubKeys);
	}

	public void appendMessagePNM(ArrayList<MessagePNM> messagePNM) {
		this.messagePNMsInServer.add(messagePNM);
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
			if (null == messagePNM) continue;
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
        for (User u: users) {
           result = result.add(u.genEncGradient());
        }
        System.out.println("Aggregate: " + result);
	}

	public void broadcastToIds(ArrayList<User> users) {
		for (User u: users) {
			u.getU3ids().addAll(receiveSigmaIds);
		}
	}

	public void receiveMessageAgg(ArrayList<MessageAgg> sendDropoutAndBeta) {
		for (MessageAgg mAgg : sendDropoutAndBeta) {
			long id = mAgg.getFromId();
			if (recoverBeta.get(id) == null) recoverBeta.put(id, new ArrayList<>());
			if (recoverSnm.get(id) == null) recoverSnm.put(id, new ArrayList<>());
			recoverBeta.get(id).add(mAgg.getBetaNtoM());
			recoverSnm.get(id).add(mAgg.getNsKm_NtoM());
		}
	}

	private BigInteger recoverBeta() {
		BigInteger beta = BigInteger.ZERO;
		for (Long l: recoverBeta.keySet()) {
			ArrayList<SecretShareBigInteger> betaShares = recoverBeta.get(l);
			if (null != betaShares) {
				SecretShareBigInteger[] shares = new SecretShareBigInteger[betaShares.size()];
				BigInteger recoverBeta = Shamir.combine(betaShares.toArray(shares), this.q);
				System.out.println("beta recover: " + recoverBeta);
				beta = beta.add(recoverBeta);
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
		return omegaXn;
	}

	public void broadcastToAggResultAndProof(ArrayList<User> users) {
	}
    
}
