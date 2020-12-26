package edu.bjut.entity;

import java.math.BigInteger;
import java.util.ArrayList;

import edu.bjut.Shamir.SecretShareBigInteger;
import edu.bjut.Shamir.Shamir;
import it.unisa.dia.gas.jpbc.Pairing;

public class Server {
	// TO DO
	private ArrayList<MessagePubKeys> msgPubkeysList;

	private	ArrayList<ArrayList<MessagePNM>> messagePNMsInServer;

	private ParamsECC paramsECC;
	private Pairing pairing;
	private BigInteger q;

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

	public Server() {
		this.msgPubkeysList = new ArrayList<MessagePubKeys>();
		this.messagePNMsInServer = new ArrayList<ArrayList<MessagePNM>>();
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
			for (int j = 0; j < Params.PARTICIPANT_NUM; ++i) {
				if (i == j) {
					users.get(i).appendMessagePMN(null);
				} else {
					users.get(i).appendMessagePMN(this.messagePNMsInServer.get(j).get(i));
				}
			}
		}
	}

	
    
}
