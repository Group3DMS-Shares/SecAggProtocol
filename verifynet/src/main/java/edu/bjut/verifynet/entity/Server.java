package edu.bjut.verifynet.entity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sun.jna.Structure;
import edu.bjut.common.shamir.SecretShareBigInteger;
import edu.bjut.common.shamir.Shamir;
import edu.bjut.common.messages.ParamsECC;
import edu.bjut.verifynet.message.*;
import edu.bjut.common.util.Params;
import edu.bjut.common.util.Utils;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
    private final static Logger LOG = LoggerFactory.getLogger(Server.class);
    private ArrayList<MessagePubKeys> msgPubKeysList;

    private ArrayList<ArrayList<MessagePNM>> messagePNMsInServer;
    private ArrayList<MessageSigma> messageSigmasInServer;
    private ArrayList<Long> receiveSigmaIds;

    private Map<Long, ArrayList<SecretShareBigInteger>> recoverBeta;
    private Map<Long, ArrayList<SecretShareBigInteger>> recoverNSk;

    private ParamsECC paramsECC;
    private Pairing pairing;
    private BigInteger q;
    private int u1Count;
    private ArrayList<ArrayList<MessageCipherPNM>> messageCipherPNMs = new ArrayList<>();

    public Server() {
        this.msgPubKeysList = new ArrayList<>();
        this.messagePNMsInServer = new ArrayList<>();
        this.messageSigmasInServer = new ArrayList<>();
        this.receiveSigmaIds = new ArrayList<>();
        this.recoverBeta = new HashMap<>();
        this.recoverNSk = new HashMap<>();
        this.u1Count = 0;
    }

    public void broadcastTo(ArrayList<User> users) {
        for (User u : users) {
            u.setBroadcastPubKeysList(this.msgPubKeysList);
        }
    }

    public void appendMessagePubKey(MessagePubKeys messagePubKeys) {
        this.msgPubKeysList.add(messagePubKeys);
        ++u1Count;
    }

    public void appendMessagePNMs(ArrayList<MessagePNM> messagePNMs) {
        this.messagePNMsInServer.add(messagePNMs);
    }
    public void appendMessageCipherPNMs(ArrayList<MessageCipherPNM> mCPNMS) {
        this.messageCipherPNMs.add(mCPNMS);
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
            shares[count++] = messagePNM.getBetaNM();
        }

        BigInteger result = Shamir.combine(shares, q);
        System.out.println(result);
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

    public void broadcastToCipherPMN(ArrayList<User> users) {
        for (int i = 0; i < Params.PARTICIPANT_NUM; ++i) {
            for (int j = 0; j < Params.PARTICIPANT_NUM; ++j) {
                if (i == j) {
                    users.get(i).appendMessageCipherPMN(null);
                } else {
                    users.get(i).appendMessageCipherPMN(this.messageCipherPNMs.get(j).get(i));
                }
            }
        }
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

    public void receiveMsgAggDropout(ArrayList<MessageDroupoutShare> sendDropout) {
        for (MessageDroupoutShare mDropoutShare : sendDropout) {
            long id = mDropoutShare.getId();
            if (recoverNSk.get(id) == null)
                recoverNSk.put(id, new ArrayList<>());
            recoverNSk.get(id).add(mDropoutShare.getNsKm_NtoM());
        }
    }

    private BigInteger recoverSnm() {
        LOG.info("dropout number: " + recoverNSk.size());
        // recover Nsk
        Map<Long, BigInteger> dropoutNsk = new HashMap<>();
        for (Entry<Long, ArrayList<SecretShareBigInteger>> e : this.recoverNSk.entrySet()) {
            ArrayList<SecretShareBigInteger> nSkmShares = e.getValue();
            if (null != nSkmShares) {
                SecretShareBigInteger[] shares = new SecretShareBigInteger[nSkmShares.size()];
                LOG.debug("nSkn recover");
                BigInteger nSk = Shamir.combine(nSkmShares.toArray(shares), this.q);
                dropoutNsk.put(e.getKey(), nSk);
            }
        }
        BigInteger omegaSnm = BigInteger.ZERO;
        for (Entry<Long, BigInteger> m: dropoutNsk.entrySet()) {
            for (Long n: receiveSigmaIds) {
                if (m.getKey().equals(n)) continue;
                // id process
                Element nPk = msgPubKeysList.get(n.intValue()).getN_pK_n().duplicate();
                BigInteger mSk = m.getValue();
                Element snm = nPk.mul(mSk);
                BigInteger s = Utils.hash2Big(snm.toString(), this.q);
                LOG.info(n + " to " + m.getKey() + ", KA agree: " + snm);

                if (m.getKey() < n) {
                    omegaSnm = omegaSnm.subtract(s);
                } else {
                    omegaSnm = omegaSnm.add(s);
                }
            }
        }
        LOG.debug("omega snm: " + omegaSnm);
        return omegaSnm;
    }

    private BigInteger recoverBeta() {
        BigInteger beta = BigInteger.ZERO;
        for (Long l: this.recoverBeta.keySet()) {
            ArrayList<SecretShareBigInteger> betaShares = this.recoverBeta.get(l);
            if (null != betaShares) {
                SecretShareBigInteger[] shares = new SecretShareBigInteger[betaShares.size()];
                LOG.debug("beta recover");
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
        BigInteger omegaBeta = recoverBeta();
        omegaXn = omegaXn.subtract(omegaBeta);
        omegaXn = omegaXn.add(recoverSnm());
        return omegaXn;
    }

    public void broadcastToAggResultAndProof(ArrayList<User> users) {
        LOG.debug("Aggregation Result: " + calculateOmeagXn());
    }

    public boolean checkU1Count(int RECOVER_K) {
        return u1Count >= RECOVER_K;
    }

}
