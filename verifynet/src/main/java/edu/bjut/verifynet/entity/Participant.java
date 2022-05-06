package edu.bjut.verifynet.entity;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;

import edu.bjut.verifynet.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import edu.bjut.common.aes.AesCipher;
import edu.bjut.common.big.BigVec;
import edu.bjut.common.messages.ParamsECC;
import edu.bjut.common.shamir.SecretShareBigInteger;
import edu.bjut.common.shamir.Shamir;
import edu.bjut.common.util.Utils;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class Participant {

    static final Logger LOG = LoggerFactory.getLogger(Participant.class);
    // pairing parameters
    private final Pairing pairing;
    private final BigInteger order;
    private final Element g;
    // for signature
    private final BigInteger duSk;
    private final Element duPk;
    private final int recoverThreshold;
    private final int userNum;
    // gradient
    private BigVec x_u = BigVec.One(1);
    private int gSize = 1;

    private final long id = Utils.incrementId();;
    private Map<Long, Element> signPubKeys = new HashMap<>();
    // round 0 keys
    private BigInteger cSk_u;
    private Element cPk_u;
    private BigInteger sSk_u;
    private Element sPk_u;
    // round 2
    private Set<Long> u2ids = new HashSet<>();
    private BigInteger b_u;
    // every s^PK_u exclude self
    private Map<Long, Element> sPubKeys = new HashMap<>();
    // every c^PK_u exclude self
    private Map<Long, Element> cPubKeys = new HashMap<>();
    // round 3
    private Set<Long> u3ids = new HashSet<>();
    private Map<Long, CipherShare> cipherShareMap = new HashMap<>();
    // time statistic
    private StopWatch stopWatch = new StopWatch("client");


    public Participant(ParamsECC ps, int gSize, int userNum) {
        this.pairing = ps.getPairing();
        this.g = ps.getGeneratorOfG1().getImmutable();
        this.order = pairing.getG1().getOrder();
        this.duSk = Utils.randomBig(order);
        this.duPk = this.g.pow(this.duSk);
        this.gSize = gSize;
        this.x_u = BigVec.One(gSize);
        this.userNum = userNum;
        this.recoverThreshold = userNum / 2 + 1;
    }

    public Element getDuPk() {
        return duPk;
    }


    public long getId() {
        return id;
    }

    public Map<Long, Element> getSignPubKeys() {
        return signPubKeys;
    }

    public void setSignPubKeys(Map<Long, Element> signPubKeys) {
        this.signPubKeys = signPubKeys;
    }

    public StopWatch getStopWatch() {
        return this.stopWatch;
    }

    private boolean verifySign(long id, Element lcPk_u, Element lsPk_u, Element sigma_u) {
        LOG.debug("verify id: " + id);
        String msg = lcPk_u.toString() + lsPk_u.toString();
        return verify(msg, this.signPubKeys.get(id), sigma_u);
    }

    private boolean verify(String msg, Element pubKey, Element sigma_u) {
        LOG.debug("verify msg: " + msg);
        Element e = Utils.hash2ElementG1(msg, this.pairing);
        Element right = this.pairing.pairing(sigma_u, this.g);
        Element left = this.pairing.pairing(e.duplicate(), pubKey);
        LOG.debug("verify: " + left.toString() + " == " + right.toString());
        return left.toString().equals(right.toString());
    }

    public MsgRound0 sendMsgRound0() {
        this.stopWatch.start("round0_send");
        // generate key pairs
        // (c^PK_u, c^SK_u),
        this.cSk_u = Utils.randomBig(order);
        this.cPk_u = this.g.pow(this.cSk_u);
        // (s^PK_u, s^SK_u), sigma_u
        this.sSk_u = Utils.randomBig(order);
        this.sPk_u = this.g.pow(this.sSk_u).getImmutable();
        LOG.debug(this.id + ", private: " + this.sSk_u + ", public: " + this.sPk_u);
        // sigma_u
        // String msg = cPk_u.toString() + sPk_u.toString();
        // LOG.info("sign msg: " + msg);
        // Element hash = Utils.hash2ElementG1(msg, this.pairing).getImmutable();
        // Element sigma_u = hash.pow(this.duSk);
        this.stopWatch.stop();
        return new MsgRound0(this.id, this.cPk_u, this.sPk_u, null);
    }

    public String getSymmetricKey(long vId) {
        var encK = this.cPubKeys.get(vId).getImmutable().pow(this.cSk_u);
        LOG.debug("aes key (" + vId + ")v id: " + encK.toString());
        return encK.toString();
    }

    public MsgRound1 sendMsgRound1(MsgResponseRound0 msgResponse) {
        this.stopWatch.start("round1_send");
        var msg = msgResponse.getPubKeys();
        for (var m : msg) {
            if (this.id == m.getId())
                continue;
            // if (!verifySign(m.getId(), m.getcPk_u(), m.getsPk_u(), m.getSigma_u()))
            //     throw new RuntimeException("Verify signature fail.");
            this.cPubKeys.put(m.getId(), m.getcPk_u());
            this.sPubKeys.put(m.getId(), m.getsPk_u());
        }
        // sample b_u
        this.b_u = Utils.randomBig(order);
        LOG.info("beta: " + this.b_u);

        // generate shares for s^SK_u
        SecureRandom random = new SecureRandom();
        SecretShareBigInteger[] b_uShares = Shamir.split(this.b_u, this.recoverThreshold, this.userNum, order,
                random);
        SecretShareBigInteger[] sSk_uShares = Shamir.split(this.sSk_u, this.recoverThreshold, this.userNum,
                order, random);

        ArrayList<CipherShare> cipherShares = new ArrayList<>();
        Iterator<Long> it = this.sPubKeys.keySet().iterator();
        for (int i = 0; i < b_uShares.length - 1; ++i) {
            var vId = it.next();
            try {
                // generate symmetric key and aes encrypt
                AesCipher aesCipher = new AesCipher(getSymmetricKey(vId), Cipher.ENCRYPT_MODE);

                ByteBuffer idBuffer = ByteBuffer.allocate(Long.BYTES).putLong(this.id);
                ByteBuffer vIdBuffer = ByteBuffer.allocate(Long.BYTES).putLong(vId);
                byte[] buNumber = b_uShares[i].getNumber().toByteArray();
                byte[] buShare = b_uShares[i].getShare().toByteArray();
                byte[] sKNumber = sSk_uShares[i].getNumber().toByteArray();
                byte[] sKShare = sSk_uShares[i].getShare().toByteArray();
                // encrypt u, v, s^SK_u, b_u,v
                var cipherShare = new CipherShare(this.id, vId, aesCipher.encrypt(idBuffer.array()),
                        aesCipher.encrypt(vIdBuffer.array()), aesCipher.encrypt(buNumber), aesCipher.encrypt(buShare),
                        aesCipher.encrypt(sKNumber), aesCipher.encrypt(sKShare));
                cipherShares.add(cipherShare);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.stopWatch.stop();
        return new MsgRound1(this.id, cipherShares);
    }

    public MsgRound2 sendMsgRound2(MsgResponseRound1 msgResponse1) {
        this.stopWatch.start("round2_send");
        ArrayList<MsgRound1> msgResponses = msgResponse1.getMsgRound1s();
        for (var m : msgResponses) {
            var uvCipherShares = m.getCiperShares();
            for (var s : uvCipherShares) {
                if (s.getvId() == this.id) {
                    this.cipherShareMap.put(s.getuId(), s);
                    this.u2ids.add(s.getuId());
                }
            }
        }
        var bUPrg = BigVec.genPRGBigVec(this.b_u.toString(), this.gSize);
        BigVec y_u = this.x_u.add(bUPrg).add(genMaskedInputCollection());
        this.stopWatch.stop();
        return new MsgRound2(this.id, y_u);
    }

    private BigVec genMaskedInputCollection() {
        LOG.debug("MaskedInputCollection.");
        BigVec p = BigVec.Zero(this.gSize);
        for (var e : sPubKeys.entrySet()) {
            Element sUV = e.getValue().getImmutable().duplicate().mul(this.sSk_u);
            LOG.debug("private: " + this.sSk_u + ", public: " + e.getValue());
            BigInteger sUVBig = Utils.hash2Big(sUV.toString(), this.order);
            LOG.info(this.id + " share with " + e.getKey() + ": " + sUVBig);
            // bUPRG vector
            var bUPrg = BigVec.genPRGBigVec(sUVBig.toString(), this.gSize);
            if (this.id > e.getKey()) {
                LOG.info("add");
                p = p.add(bUPrg);
            } else {
                LOG.info("subtract");
                p = p.subtract(bUPrg);
            }
        }
        return p;
    }

    public MsgRound3 sendMsgRound3(MsgResponseRound2 msgResponse2) {
        this.stopWatch.start("round3_send");
        var idList = msgResponse2.getU3ids();
        this.u3ids.addAll(idList);
        StringBuilder stringBuilder = new StringBuilder();
        idList.forEach(stringBuilder::append);
        String msg = stringBuilder.toString();
        // Element signature = Utils.hash2ElementG1(msg, this.pairing).mul(this.duSk);
        this.stopWatch.stop();
        return  new MsgRound3(this.id, null);
    }

    public MsgRound4 sendMsgRound4(MsgResponseRound3 msgResponse3) {
        this.stopWatch.start("round4_send");
        // verify
        for (var s : msgResponse3.getSigmas()) {
            StringBuilder stringBuilder = new StringBuilder();
            this.u3ids.forEach(stringBuilder::append);
            LOG.trace("Verify id list in Round 4");
            // if (!verify(stringBuilder.toString(), this.signPubKeys.get(s.getId()), s.getSignature())) {
            //     throw new RuntimeException("Verify failure in Round 4");
            // }
        }

        ArrayList<BetaShare> betaShares = new ArrayList<>();
        ArrayList<UShare> uShares = new ArrayList<>();
        for (var x : u2ids) {
            // decrypt the shares
            boolean needSk = false;
            boolean needBu = false;
            try {
                if (u3ids.contains(x)) {
                    needBu = true;
                } else {
                    needSk = true;
                }
                var cipherShare = cipherShareMap.get(x);
                UVShare uvShare = decryptShare(cipherShare, getSymmetricKey(x), needSk);
                if (needBu) {
                    BetaShare betaShare = new BetaShare(x, uvShare.getB_uShare());
                    betaShares.add(betaShare);
                }
                if (needSk) {
                    UShare uShare = new UShare(x, uvShare.getSkShare());
                    uShares.add(uShare);
                }
            } catch (Exception e) {
                throw new RuntimeException("decrypt shares error: " + e.getMessage());
            }
        }
        this.stopWatch.stop();
        return new MsgRound4(betaShares, uShares);
    }

    private UVShare decryptShare(CipherShare cipherShare, String symmetricKey, boolean needSu) throws
            Exception {
        AesCipher aesCipher = new AesCipher(symmetricKey, Cipher.DECRYPT_MODE);
        byte[] cUid = cipherShare.getcUId();
        byte[] cVid = cipherShare.getcVId();
        byte[] cbuNumber = cipherShare.getBuNumber();
        byte[] cbuShare = cipherShare.getBuShare();
        byte[] csKNumber = cipherShare.getSuNumber();
        byte[] csKShare = cipherShare.getSuShare();
        var uId = ByteBuffer.wrap(aesCipher.decrypt(cUid)).getLong();
        var vId = ByteBuffer.wrap(aesCipher.decrypt(cVid)).getLong();
        SecretShareBigInteger b_uShare = new SecretShareBigInteger(new BigInteger(aesCipher.decrypt(cbuNumber)),
                new BigInteger(aesCipher.decrypt(cbuShare)));
        SecretShareBigInteger svuShare = null;
        if (needSu) {
            svuShare = new SecretShareBigInteger(new BigInteger(aesCipher.decrypt(csKNumber)), new BigInteger(aesCipher.decrypt(csKShare)));
        }
        return new UVShare(uId, vId, b_uShare, svuShare);
    }
}