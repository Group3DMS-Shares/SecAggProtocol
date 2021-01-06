package edu.bjut.verifynet.entity;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;

import edu.bjut.common.aes.AesCipher;
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

import javax.crypto.Cipher;

public class User {

    static final Logger LOG = LoggerFactory.getLogger(User.class);

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
    private ArrayList<MessageCipherPNM> cipherPmnList = new ArrayList<>();

    public User() {
        // this.id = Utils.randomLong();
        this.id = Utils.incrementId();
        this.pmnList = new ArrayList<>();
        this.setU3ids(new ArrayList<>());
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
        // this.beta = Utils.randomBig(q);
        this.beta = BigInteger.ONE;
        LOG.debug("Beta: " + this.beta);
        SecureRandom random = new SecureRandom();
        SecretShareBigInteger[] shares = Shamir.split(beta, Params.RECOVER_K, Params.PARTICIPANT_NUM - 1, q, random);
        return shares;
    }

    public SecretShareBigInteger[] genN_SK_nShares() {
        SecureRandom random = new SecureRandom();
        SecretShareBigInteger[] shares = Shamir.split(this.n_sK_n, Params.RECOVER_K, Params.PARTICIPANT_NUM - 1, q, random);
        return shares;
    }

    public String genKA_Agree(Element p_Pk_m) {
        Element snm = p_Pk_m.duplicate().mul(this.p_sK_n);
        return snm.toString();
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
            LOG.debug("add ---> share: " + this.id + " to " + broadcastPubKeysList.get(i).getIdm() + ": " + tem.toString());
            pi = pi.add(tem);
        }
        for (int i = indexSelf + 1; i < broadcastPubKeysList.size(); i++) {
            BigInteger tem = genKA_AgreeMaskedInput(broadcastPubKeysList.get(i).getN_pK_n());
            LOG.debug("subtract --- >share: " + this.id + " to " + broadcastPubKeysList.get(i).getIdm() + ": " + tem.toString());
            pi = pi.subtract(tem);
        }
        return pi;
    }
     
    public BigInteger genEncGradient() {
        LOG.info(" User Upload: " + this.id);
        BigInteger x_n = BigInteger.ONE;
        return x_n.add(genMaskedInputI()).add(genMaskedInputII());
    }

    public MessageSigma genMessageSigma() {
        MessageSigma messageSigma = new MessageSigma(genEncGradient());
        messageSigma.setId(this.id);
        return messageSigma;
    }

    public ArrayList<MessagePNM> genMsgPNMs() {
        if (broadcastPubKeysList.size() < Params.RECOVER_K)
            throw new RuntimeException("get the num of the public keys smaller than recovery threshold");

        SecretShareBigInteger[] shareBeta = genBetaShares();
        SecretShareBigInteger[] shareN_Sk_n_m = genN_SK_nShares();
        int index = 0;
        ArrayList<MessagePNM> messagePNMLists= new ArrayList<>();

        for (MessagePubKeys messagePubKeys: broadcastPubKeysList) {
            if (messagePubKeys.getIdm() != this.id) {
                MessagePNM messagePNM = new MessagePNM(this.id, messagePubKeys.getIdm(),
                        shareN_Sk_n_m[index], shareBeta[index]);
                messagePNMLists.add(messagePNM);
                index++;
            } else {
                messagePNMLists.add(null);
            }
        }
        return messagePNMLists;
    }


    public ArrayList<MessageCipherPNM> genMsgCipherPNMs() {
        if (broadcastPubKeysList.size() < Params.RECOVER_K)
            throw new RuntimeException("get the num of the public keys smaller than recovery threshold");

        SecretShareBigInteger[] shareBeta = genBetaShares();
        SecretShareBigInteger[] shareN_Sk_n_m = genN_SK_nShares();
        int index = 0;
        ArrayList<MessageCipherPNM> messageCipherPNMList= new ArrayList<>();

        for (MessagePubKeys messagePubKeys: broadcastPubKeysList) {
            if (messagePubKeys.getIdm() != this.id) {
                MessagePNM messagePNM = new MessagePNM(this.id, messagePubKeys.getIdm(),
                        shareN_Sk_n_m[index], shareBeta[index]);
                String key = genKA_Agree(messagePubKeys.getP_pK_n());
                LOG.debug(this.id + " to " + messagePubKeys.getIdm() + " aes key: " + key);
                MessageCipherPNM messageCipherPNM = encrypt(messagePNM, messagePubKeys.getIdm(), key);
                messageCipherPNMList.add(messageCipherPNM);
                index++;
            } else {
                messageCipherPNMList.add(null);
            }
        }
        return messageCipherPNMList;
    }

    private MessageCipherPNM encrypt(MessagePNM messagePNM, long endId, String key) {
        MessageCipherPNM messageCipherPNM;
        try {
            AesCipher aesCipher = new AesCipher(key, Cipher.ENCRYPT_MODE);
            var fromId = messagePNM.getFromIdN();
            var toId = messagePNM.getToIdM();
            var  betaNumber = messagePNM.getBetaNM().getNumber();
            var  betaShare = messagePNM.getBetaNM().getShare();
            var  nSkNumber = messagePNM.getnSkNM().getNumber();
            var  nSkShare = messagePNM.getnSkNM().getShare();

            messageCipherPNM = new MessageCipherPNM(this.id, endId,
                    aesCipher.encrypt(ByteBuffer.allocate(Long.BYTES).putLong(fromId).array()),
                    aesCipher.encrypt(ByteBuffer.allocate(Long.BYTES).putLong(toId).array()),
                    aesCipher.encrypt(betaNumber.toByteArray()),
                    aesCipher.encrypt(betaShare.toByteArray()),
                    aesCipher.encrypt(nSkNumber.toByteArray()),
                    aesCipher.encrypt(nSkShare.toByteArray()));
        } catch (Exception e) {
            throw new RuntimeException("encrypt exception: " + e.getMessage());
        }
        return messageCipherPNM;
    }

    public MessagePNM decrypt(MessageCipherPNM messageCipherPNM, String key) {
        MessagePNM messagePNM;
        try {
            AesCipher aesCipher = new AesCipher(key, Cipher.DECRYPT_MODE);
            var fromIdBytes = messageCipherPNM.getcFromIdN();
            var toIdBytes = messageCipherPNM.getcToIdM();
            var  betaNumber = messageCipherPNM.getBetaNumber();
            var  betaShare = messageCipherPNM.getBetaShare();
            var  nSkNumber = messageCipherPNM.getnSkNumber();
            var  nSkShare = messageCipherPNM.getnSkShare();
            var fromId = ByteBuffer.wrap(aesCipher.decrypt(fromIdBytes)).getLong();
            var toId = ByteBuffer.wrap(aesCipher.decrypt(toIdBytes)).getLong();
            SecretShareBigInteger beta = new SecretShareBigInteger( new BigInteger(aesCipher.decrypt(betaNumber)),
                    new BigInteger(aesCipher.decrypt(betaShare)));
            SecretShareBigInteger sk = new SecretShareBigInteger(new BigInteger(aesCipher.decrypt(nSkNumber)),
                    new BigInteger(aesCipher.decrypt(nSkShare)));
            messagePNM = new MessagePNM(fromId, toId, sk, beta);
        } catch (Exception e) {
            throw new RuntimeException("decrypt exception: " + e.getMessage());
        }
        return messagePNM;
    }

    public void appendMessagePMN(MessagePNM messagePMN) {
        this.pmnList.add(messagePMN);
    }

    public void appendMessageCipherPMN(MessageCipherPNM messagePNM) {
        this.cipherPmnList.add(messagePNM);
    }

    public void setKeys(MessageKeys msgKeys) {
        this.delta = msgKeys.getDelta();
        this.rho = msgKeys.getRho();

        this.n_sK_n = msgKeys.getN_sK_n();
        this.n_pK_n = msgKeys.getN_pK_n();

        this.p_sK_n = msgKeys.getP_sK_n();
        this.p_pK_n = msgKeys.getP_pK_n();
    }

    public ArrayList<MessageBetaShare> sendCBetaShare() {
        ArrayList<MessageBetaShare> betaShares = new ArrayList<>();
        for (MessageCipherPNM cipherPNM: this.cipherPmnList) {
            if (null == cipherPNM) continue;
            var uId = cipherPNM.getFromIdN();
            if (u3ids.contains(uId)) {
                String key = genKA_Agree(broadcastPubKeysList.get((int) uId).getP_pK_n());
                LOG.debug(this.id + " to " +  uId + " aes key: " + key);
                var pnm = decrypt(cipherPNM, key);
                MessageBetaShare betaShare = new MessageBetaShare(pnm.getFromIdN(), pnm.getBetaNM());
                betaShares.add(betaShare);
            }
        }
        return betaShares;
    }

    public ArrayList<MessageDroupoutShare> sendCDropoutAndBetaShare(ArrayList<Long> dropOutUsers) {
        ArrayList<MessageDroupoutShare> dropoutShares = new ArrayList<>();
        for (MessageCipherPNM cipherPNM: this.cipherPmnList ) {
            if (null == cipherPNM) continue;
            var uId = cipherPNM.getFromIdN();
            if (dropOutUsers.contains(uId)) {
                var pnm = decrypt(cipherPNM, genKA_Agree(broadcastPubKeysList.get((int) uId).getP_pK_n()));
                MessageDroupoutShare dropoutShare = new MessageDroupoutShare(pnm.getFromIdN(), pnm.getnSkNM());
                dropoutShares.add(dropoutShare);
            }
        }
        return dropoutShares;
    }

    public ArrayList<MessageBetaShare> sendBetaShare() {
        ArrayList<MessageBetaShare> mBetaShares = new ArrayList<>();
        for (MessagePNM mPnm: pmnList) {
            if (null != mPnm && u3ids.contains(mPnm.getFromIdN())) {
                MessageBetaShare mBetaShare = new MessageBetaShare(mPnm.getFromIdN(), mPnm.getBetaNM());
                mBetaShares.add(mBetaShare);
            }
        }
        return mBetaShares;
    }


    public ArrayList<MessageDroupoutShare> sendDropoutAndBetaShare(ArrayList<Long> droupOutUsers) {
        ArrayList<MessageDroupoutShare> mDropoutShares = new ArrayList<>();
        for (MessagePNM mPnm: pmnList) {
            if (null != mPnm && droupOutUsers.contains(mPnm.getFromIdN())) {
                MessageDroupoutShare mDropoutShare = new MessageDroupoutShare(mPnm.getFromIdN(), mPnm.getnSkNM());
                mDropoutShares.add(mDropoutShare);
            }
        }
        return mDropoutShares;
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
        // TODO verify
        return true;
    }

}

