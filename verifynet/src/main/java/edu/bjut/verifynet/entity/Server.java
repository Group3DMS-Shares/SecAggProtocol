package edu.bjut.verifynet.entity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.bjut.common.big.BigVec;
import edu.bjut.common.messages.ParamsECC;
import edu.bjut.common.shamir.SecretShareBigInteger;
import edu.bjut.common.shamir.Shamir;
import edu.bjut.verifynet.message.*;
import edu.bjut.common.util.PRG;
import edu.bjut.common.util.Params;
import edu.bjut.common.util.Utils;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

public class Server {
    private final static Logger LOG = LoggerFactory.getLogger(Server.class);

    private ArrayList<MessagePubKeys> msgPubKeysList = new ArrayList<>();

    private ArrayList<ArrayList<MessagePNM>> messagePNMsInServer = new ArrayList<>();
    private ArrayList<MessageSigma> messageSigmasInServer = new ArrayList<>();
    private ArrayList<Long> receiveSigmaIds = new ArrayList<>();

    private Map<Long, ArrayList<SecretShareBigInteger>> recoverBeta = new HashMap<>();
    private Map<Long, ArrayList<SecretShareBigInteger>> recoverNSk = new HashMap<>();

    private Pairing pairing;
    private BigInteger q;
    private int u1Count = 0;
    private ArrayList<ArrayList<MessageCipherPNM>> messageCipherPNMs = new ArrayList<>();
    private StopWatch stopWatch = new StopWatch("client");

    public Server() { }

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
        LOG.info(result.toString());
    }

    public void setParamsECC(ParamsECC paramsECC) {
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
        this.stopWatch.start("round1_broadcast");
        for (int i = 0; i < Params.PARTICIPANT_NUM; ++i) {
            for (int j = 0; j < Params.PARTICIPANT_NUM; ++j) {
                if (i == j) {
                    users.get(i).appendMessageCipherPMN(null);
                } else {
                    users.get(i).appendMessageCipherPMN(this.messageCipherPNMs.get(j).get(i));
                }
            }
        }
        this.stopWatch.stop();
    }

    public void broadcastToIds(ArrayList<User> users) {
        this.stopWatch.start("round2_broadcast");
        for (User u : users) {
            u.getU3ids().addAll(receiveSigmaIds);
        }
        this.stopWatch.stop();
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

    private BigVec recoverSnm(int gSize) {
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
        BigVec omegaSnm = BigVec.Zero(gSize);
        for (Entry<Long, BigInteger> m : dropoutNsk.entrySet()) {
            for (Long n : receiveSigmaIds) {
                if (m.getKey().equals(n))
                    continue;
                // id process
                Element nPk = msgPubKeysList.get(n.intValue()).getN_pK_n().duplicate();
                BigInteger mSk = m.getValue();
                Element snm = nPk.mul(mSk);
                BigInteger s = Utils.hash2Big(snm.toString(), this.q);
                LOG.info(n + " to " + m.getKey() + ", KA agree: " + snm);
                PRG p = new PRG(s.toString());
                var bigs = p.genBigs(gSize);
                if (m.getKey() < n) {
                    omegaSnm = omegaSnm.subtract(new BigVec(bigs));
                } else {
                    omegaSnm = omegaSnm.add(new BigVec(bigs));
                }
            }
        }
        LOG.debug("omega snm: " + omegaSnm);
        return omegaSnm;
    }

    public BigVec calculateOmeagXn() {
        this.stopWatch.start("agg_1");
        int gLen = messageSigmasInServer.get(0).getX_n_hat().size();
        BigVec omegaXn = BigVec.Zero(gLen);

        for (MessageSigma mSigma: messageSigmasInServer) {
            omegaXn = omegaXn.add(mSigma.getX_n_hat());
        }
        this.stopWatch.stop();
        this.stopWatch.start("agg_2");

        for (var e: this.recoverBeta.entrySet()) {
            var v = e.getValue();
            SecretShareBigInteger[] shares = new SecretShareBigInteger[v.size()];
            var puBig = Shamir.combine(v.toArray(shares), this.q);
            LOG.debug("recoverBeta " + e.getKey() + " : " + puBig);
            PRG prg = new PRG(puBig.toString());
            var puBigArray = prg.genBigs(gLen);
            omegaXn = omegaXn.add(new BigVec(puBigArray));
        }

        this.stopWatch.stop();
        this.stopWatch.start("agg_3");
        omegaXn = omegaXn.add(recoverSnm(gLen));
        this.stopWatch.stop();
        return omegaXn;
    }

    public void broadcastToAggResultAndProof(ArrayList<User> users) {
        // this.stopWatch.start("agg_result");
        var r = calculateOmeagXn();
        LOG.info("Aggregation Result: " + r);
        // this.stopWatch.stop();
    }

    public boolean checkU1Count(int RECOVER_K) {
        return u1Count >= RECOVER_K;
    }

    public StopWatch getStopWatch() {
        return this.stopWatch;
    }

}
