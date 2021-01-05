package edu.bjut.psecagg.entity;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.bjut.common.aes.AesCipher;
import edu.bjut.common.shamir.SecretShareBigInteger;
import edu.bjut.common.shamir.Shamir;
import edu.bjut.common.messages.ParamsECC;
import edu.bjut.common.util.Params;
import edu.bjut.common.util.Utils;
import edu.bjut.psecagg.messages.*;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Participant {

    static final Logger LOG = LoggerFactory.getLogger(Participant.class);

    // pairing parameters
    private final Pairing pairing;
    private final BigInteger order;
    private final Element g;

    // for signature
    private final BigInteger duSk;
    private final Element duPk;

    // gradient
    private final BigInteger x_u;

    private final long id;
    private Map<Long, Element> signPubKeys;

    // round 0 keys
    private BigInteger cSk_u;
    private Element cPk_u;

    private BigInteger sSk_u;
    private Element sPk_u;

    // round 2
    private final ArrayList<Long> u2ids;
    private BigInteger b_u;
    // every s^PK_u exclude self
    private final Map<Long, Element> sPubKeys;
    // every c^PK_u exclude self
    private final Map<Long, Element> cPubKeys = new HashMap<>();
    private final ArrayList<UVShare> uvShareList = new ArrayList<>();
    // round 3
    private final ArrayList<Long> u3ids;

    public Element getDuPk() {
        return duPk;
    }

    public Participant(ParamsECC ps) {

        this.id = Utils.incrementId();
        this.pairing = ps.getPairing();
        this.g = ps.getGeneratorOfG1().getImmutable();
        this.order = pairing.getG1().getOrder();
        this.x_u = BigInteger.ONE;

        this.duSk = Utils.randomBig(order);
        this.duPk = this.g.duplicate().pow(this.duSk);

        this.signPubKeys = new HashMap<>();
        this.sPubKeys = new HashMap<>();
        this.u2ids = new ArrayList<>();
        this.u3ids = new ArrayList<>();
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


    private boolean verifySign(long id, Element lcPk_u, Element lsPk_u, Element sigma_u) {
        StringBuilder stringBuilder = new StringBuilder();
        String msg = stringBuilder.append(lcPk_u.toString())
                .append(lsPk_u.toString())
                .toString();
        return verify(msg, this.signPubKeys.get(id), sigma_u);
    }

    private boolean verify(String msg, Element pubKey, Element sigma_u) {
        LOG.trace("verify msg: " + msg);
        Element e = Utils.hash2ElementG1(msg, this.pairing);
        Element right = this.pairing.pairing(sigma_u, g);
        Element left = this.pairing.pairing(e.duplicate(), pubKey);
        LOG.trace("verify: " + left.toString() + ", " + right.toString());
        return left.toString().equals(right.toString());
    }

    public MsgRound0 sendMsgRound0() {
        // generate key pairs
        // (c^PK_u, c^SK_u),
        this.cSk_u = Utils.randomBig(order);
        this.cPk_u = g.duplicate().pow(this.cSk_u);
        // (s^PK_u, s^SK_u), sigma_u
        this.sSk_u = Utils.randomBig(order);
        this.sPk_u = g.duplicate().pow(this.sSk_u).getImmutable();
        LOG.debug(this.id + ", private: " + this.sSk_u + ", public: " + this.sPk_u);
        // sigma_u
        String msg = cPk_u.toString() +
                sPk_u.toString();
        LOG.info("sign msg: " + msg);
        Element hash = Utils.hash2ElementG1(msg, this.pairing).getImmutable();
        Element sigma_u = hash.pow(this.duSk);
        return new MsgRound0(this.id, this.cPk_u, this.sPk_u, sigma_u);
    }

    public String getSymmetricKey(long vId) {
        var encK = this.cPubKeys.get(vId).pow(this.cSk_u);
        LOG.debug("aes key (" + vId + ")v id: " + encK.toString());
        return encK.toString();
    }


    public MsgRound1 sendMsgRound1(MsgResponseRound0 msgResponse) {
        var msg = msgResponse.getPubKeys();
        for (var m : msg) {
            if (this.id == m.getId()) continue;
            if (!verifySign(m.getId(), m.getcPk_u(), m.getsPk_u(), m.getSigma_u()))
                throw  new RuntimeException("Verify signature fail.");
            this.sPubKeys.put(m.getId(), m.getsPk_u());
            this.cPubKeys.put(m.getId(), m.getcPk_u());
        }
        // sample b_u
        this.b_u = Utils.randomBig(order);
        LOG.info("beta: " + this.b_u);

        // generate shares for s^SK_u
        SecureRandom random = new SecureRandom();
        SecretShareBigInteger[] b_uShares = Shamir.split(this.b_u, Params.RECOVER_K,
                Params.PARTICIPANT_NUM - 1, order, random);
        SecretShareBigInteger[] sSk_uShares = Shamir.split(this.sSk_u, Params.RECOVER_K,
                Params.PARTICIPANT_NUM-1, order, random);

        // TODO encrypt u, v, s^SK_u, b_u,v
        ArrayList<UVShare> uvShareList = new ArrayList<>();
        ArrayList<CipherShare> ciperShares = new ArrayList<>();
        Iterator<Long> it = this.sPubKeys.keySet().iterator();
        for (int i = 0; i < b_uShares.length; ++i){
            var vId = it.next();
            try {
                // generate symmetric key and aes encrypt
                AesCipher aesCipher = new AesCipher(getSymmetricKey(vId));
                UVShare uvShare = new UVShare(this.id, vId,b_uShares[i], sSk_uShares[i]);
                uvShareList.add(uvShare);
                //
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new MsgRound1(this.id, uvShareList, ciperShares);
    }


    public MsgRound2 sendMsgRound2(MsgResponseRound1 msgResponse1) {
        ArrayList<MsgRound1> msgResponses = msgResponse1.getMsgRound1s();
        for (var m : msgResponses) {
            var uvShares = m.getUvSharesList();
            for (var s : uvShares) {
                if (s.getvId() == this.id) {
                    this.uvShareList.add(s);
                    u2ids.add(s.getuId());
                }
            }
        }
        // TODO store uvShares
        BigInteger y_u = this.x_u.add(this.b_u).add(genMaskedInputCollection());

        return new MsgRound2(this.id, y_u);
    }

    private BigInteger genMaskedInputCollection() {
        LOG.debug("MaskedInputCollection.");
        BigInteger p = BigInteger.ZERO;
        for (var e : sPubKeys.entrySet()) {
            Element sUV = e.getValue().getImmutable().duplicate().mul(this.sSk_u);
            LOG.debug("private: " + this.sSk_u + ", public: " + e.getValue());
            BigInteger sUVBig = Utils.hash2Big(sUV.toString(), this.order);
            LOG.info(this.id + " share with " + e.getKey() + ": " + sUVBig);
            if (this.id > e.getKey()) {
                LOG.info("add");
                p = p.add(sUVBig);
            } else {
                LOG.info("subtract");
                p = p.subtract(sUVBig);
            }
        }
        return p;
    }

    public MsgRound3 sendMsgRound3(MsgResponseRound2 msgResponse2) {
        var idList = msgResponse2.getU3ids();
        this.u3ids.addAll(idList);
        StringBuilder stringBuilder = new StringBuilder();
        idList.forEach(stringBuilder::append);
        String msg = stringBuilder.toString();
        Element signature = Utils.hash2ElementG1(msg, this.pairing).mul(this.duSk);
        return  new MsgRound3(this.id, signature);
    }

    public MsgRound4 sendMsgRound4(MsgResponseRound3 msgResponse3) {
        // verify
        for (var s : msgResponse3.getSigmas()) {
            StringBuilder stringBuilder = new StringBuilder();
            this.u3ids.forEach(stringBuilder::append);
            LOG.trace("Verify id list in Round 4");
            if (!verify(stringBuilder.toString(), this.signPubKeys.get(s.getId()), s.getSignature())) {
                throw new RuntimeException("Verify failure in Round 4");
            }

        }

        ArrayList<BetaShare> betaShares = new ArrayList<BetaShare>();
        ArrayList<SvuShare> svuShares = new ArrayList<SvuShare>();
        for (var u : this.uvShareList) {
            long uId = u.getuId();
            if (u3ids.contains(uId)) {
                BetaShare betaShare = new BetaShare(uId, u.getB_uShare());
                betaShares.add(betaShare);
            } else if (u2ids.contains(uId)) {
                LOG.debug("Dropout user is " + uId);
                SvuShare svuShare = new SvuShare(uId, u.getSkShare());
                svuShares.add(svuShare);
            }
        }
        return new MsgRound4(betaShares, svuShares);
    }
}
