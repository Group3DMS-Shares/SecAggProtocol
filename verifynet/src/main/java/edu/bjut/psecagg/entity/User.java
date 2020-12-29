package edu.bjut.psecagg.entity;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

import edu.bjut.common.Shamir.SecretShareBigInteger;
import edu.bjut.common.Shamir.Shamir;
import edu.bjut.verifynet.message.MessageBetaShare;
import edu.bjut.verifynet.message.MessageDroupoutShare;
import edu.bjut.verifynet.message.MessageKeys;
import edu.bjut.verifynet.message.MessagePNM;
import edu.bjut.verifynet.message.MessagePubKeys;
import edu.bjut.verifynet.message.MessageSigma;
import edu.bjut.common.util.Params;
import edu.bjut.common.util.Utils;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class User {
    private BigInteger delta;
    private BigInteger rho;

    private BigInteger n_sK_n;
    private Element n_pK_n;

    private BigInteger p_sK_n;
    private Element p_pK_n;
    private long id;
    private ArrayList<MessagePubKeys> broadcastPubKeysList;
    private ArrayList<Long> u3ids;

    private Pairing pairing;
    private Element g;
    private BigInteger q;

    private BigInteger beta;
    private ArrayList<MessagePNM> pmnList;

    public User() {
        // this.id = Utils.randomlong();
        this.id = Utils.incrementId();
        this.pmnList = new ArrayList<MessagePNM>();
        this.setU3ids(new ArrayList<Long>());
    }

    public ArrayList<Long> getU3ids() {
        return u3ids;
    }

    public void setU3ids(ArrayList<Long> u3ids) {
        this.u3ids = u3ids;
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
        System.out.println("Beta: " + this.beta);
        SecureRandom random = new SecureRandom();
        SecretShareBigInteger[] shares = Shamir.split(beta, Params.RECOVER_K, Params.PARTICIPANT_NUM - 1, q, random);
        return shares;
    }

    public SecretShareBigInteger[] genN_SK_nShares() {
        SecureRandom random = new SecureRandom();
        SecretShareBigInteger[] shares = Shamir.split(this.n_sK_n, Params.RECOVER_K, Params.PARTICIPANT_NUM - 1, q, random);
        return shares;
    }

    public void genKA_Agree(Element p_Pk_m) {
        Element snm = p_Pk_m.duplicate().mul(this.p_sK_n);
        String snm_string = snm.toString();    
    }

    private BigInteger genKA_AgreeMaskedInput(Element n_Pk_m) {
        Element snm = n_Pk_m.duplicate().mul(this.n_sK_n);
        return Utils.hash2Big(snm.toString(), this.q);
    }

    private BigInteger genMaskedInputI() {
        return this.beta;
    }

    private BigInteger genMaskedInputII() {
        BigInteger pi = BigInteger.ZERO;
        int indexSelf = -1;
        for (int i = 0; i < broadcastPubKeysList.size(); ++i) {
            if (this.broadcastPubKeysList.get(i).getIdm() == this.id) indexSelf = i;
        }

        if (-1 == indexSelf) throw new RuntimeException("can't find indexSelf");

        for (int i = 0; i < indexSelf; i++) {
            BigInteger tem = genKA_AgreeMaskedInput(broadcastPubKeysList.get(i).getN_pK_n());
            System.out.println("add ---> share: " + this.id + " to " + broadcastPubKeysList.get(i).getIdm() + ": " + tem.toString());
            pi = pi.add(tem);
        }
        for (int i = indexSelf + 1; i < broadcastPubKeysList.size(); i++) {
            BigInteger tem = genKA_AgreeMaskedInput(broadcastPubKeysList.get(i).getN_pK_n());
            System.out.println("subtract --- >share: " + this.id + " to " + broadcastPubKeysList.get(i).getIdm() + ": " + tem.toString());
            pi = pi.subtract(tem);
        }
        return pi;
    }
     
    public BigInteger genEncGradient() {
        System.out.println(" User Upload: " + this.id);
        BigInteger x_n = BigInteger.ONE;
        BigInteger x_n_hat = x_n.add(genMaskedInputI()).add(genMaskedInputII());
        return x_n_hat;
    }

    public MessageSigma genMessageSigma() {
        MessageSigma messageSigma = new MessageSigma(genEncGradient());
        messageSigma.setId(this.id);
        return messageSigma;
    }

    public ArrayList<MessagePNM> genMsgPNMs() {
        if (broadcastPubKeysList.size() < Params.RECOVER_K)
            throw new RuntimeException("get the num of the pubkeys smaller than recovery threshold");

        SecretShareBigInteger[] shareBeta = genBetaShares();
        SecretShareBigInteger[] shareN_Sk_n_m = genN_SK_nShares();
        int index = 0;
        ArrayList<MessagePNM> messagePNMLists= new ArrayList<MessagePNM>();

        for (MessagePubKeys messagePubKeys: broadcastPubKeysList) {
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

        this.n_sK_n = msgKeys.getN_sK_n();
        this.n_pK_n = msgKeys.getN_pK_n();

        this.p_sK_n = msgKeys.getP_sK_n();
        this.p_pK_n = msgKeys.getP_pK_n();
    }

    public ArrayList<MessageBetaShare> sendBetaShare() {
        ArrayList<MessageBetaShare> mBetaShares = new ArrayList<>();
        for (MessagePNM mPnm: pmnList) {
            if (null != mPnm && u3ids.contains(mPnm.getFromIdN())) {
                MessageBetaShare mBetaShare = new MessageBetaShare(mPnm.getFromIdN(), mPnm.getBeta_n_m());
                mBetaShares.add(mBetaShare);
            }
        }
        return mBetaShares;
    }


    public ArrayList<MessageDroupoutShare> sendDropoutAndBetaShare(ArrayList<Long> droupOutUsers) {
        ArrayList<MessageDroupoutShare> mDroupoutShares = new ArrayList<>();
        for (MessagePNM mPnm: pmnList) {
            if (null != mPnm && droupOutUsers.contains(mPnm.getFromIdN())) {
                MessageDroupoutShare mDroupoutShare = new MessageDroupoutShare(mPnm.getFromIdN(), mPnm.getN_Sk_n_m());
                mDroupoutShares.add(mDroupoutShare);
            }
        }
        return mDroupoutShares;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ArrayList<MessagePubKeys> getBroadcastPubKeysList() {
        return broadcastPubKeysList;
    }

    public void setBroadcastPubKeysList(ArrayList<MessagePubKeys> broadcastPubKeysList) {
        this.broadcastPubKeysList = broadcastPubKeysList;
    }

    public boolean verifyAggregation() {
        return false;
    }
}

