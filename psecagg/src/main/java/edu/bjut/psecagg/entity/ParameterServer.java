package edu.bjut.psecagg.entity;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.bjut.common.shamir.SecretShareBigInteger;
import edu.bjut.common.shamir.Shamir;
import edu.bjut.common.messages.ParamsECC;
import edu.bjut.common.util.Params;
import edu.bjut.common.util.Utils;
import edu.bjut.psecagg.messages.*;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterServer {

    static final Logger LOG = LoggerFactory.getLogger(ParameterServer.class);

    // pairing parameters
    private Pairing pairing;
    private Element g;
    private BigInteger order;

    // Round 0
    private int u1Count = 0;
    private ArrayList<MsgRound0> msgRound0s = new ArrayList<>();
    private Map<Long, Element> cPk_uMap = new HashMap<>();
    private Map<Long, Element> sPk_uMap = new HashMap<>();

    // Round 1
    private int u2Count = 0;
    private ArrayList<Long> u2ids = new ArrayList<>();
    private ArrayList<MsgRound1> allMsgRound1s = new ArrayList<>();

    // Round 2
    private int u3Count = 0;
    private ArrayList<Long> u3Ids = new ArrayList<>();
    private ArrayList<BigInteger> y_uList = new ArrayList<>();

    // Round 3
    private int u4Count = 0;
    private ArrayList<MsgRound3> u4Sigmas = new ArrayList<>();

    // Round 4
    Map<Long, ArrayList<SecretShareBigInteger>> buMap = new HashMap<>();
    Map<Long, ArrayList<SecretShareBigInteger>> svnMap = new HashMap<>();

    public ParameterServer() throws IOException {
        this.pairing = PairingFactory.getPairing("aggVote1.properties");
        this.order = pairing.getG1().getOrder();
        this.g = this.pairing.getG1().newRandomElement().getImmutable();
    }

    public ParamsECC getParamsECC() {
        ParamsECC ps = new ParamsECC(this.pairing, this.g);
        return ps;
    }


    public void recvMsgRound0(MsgRound0 msgRound0) {
        msgRound0s.add(msgRound0);
        this.sPk_uMap.put(msgRound0.getId(), msgRound0.getsPk_u());
        this.cPk_uMap.put(msgRound0.getId(), msgRound0.getcPk_u());
    }

	public MsgResponseRound0 sendMsgResponseRound0() {
        this.u1Count = msgRound0s.size();
        // TODO t-out-of-n check
        assert(this.u1Count >= Params.RECOVER_K);
        return new MsgResponseRound0(msgRound0s);
	}


    public void recvMsgRound1(MsgRound1 msgRound1) {
        this.allMsgRound1s.add(msgRound1);
        this.u2ids.add(msgRound1.getId());
    }

    public MsgResponseRound1 sendMsgResponseRound1() {
        this.u2Count = this.allMsgRound1s.size();
        // TODO t-out-of-n check
        assert(this.u1Count >= Params.RECOVER_K);
        return new MsgResponseRound1(this.allMsgRound1s);
    }

    public void recvMsgRound2(MsgRound2 msgRound2) {
        this.u3Ids.add(msgRound2.getId());
        this.y_uList.add(msgRound2.getY());
    }

    public MsgResponseRound2 sendMsgResponseRound2() {
        // TODO t-out-of-n check
        this.u3Count = this.u3Ids.size();
        return new MsgResponseRound2(u3Ids);
    }

    public void recvMsgRound3(MsgRound3 msgRound3) {
        this.u4Sigmas.add(msgRound3);
    }

    public MsgResponseRound3 sendMsgResponseRound3() {
        // TODO t-out-of-n check
        this.u4Count = this.u4Sigmas.size();
        return new MsgResponseRound3(this.u4Sigmas);
    }

    public void recvMsgRound4(MsgRound4 msgRound4) {
        var betaShares = msgRound4.getBetaShares();
        var svnShares = msgRound4.getSvuShares();
        betaShares.forEach(x-> {
            if (this.buMap.get(x.getId()) == null) this.buMap.put(x.getId(), new ArrayList<>());
            this.buMap.get(x.getId()).add(x.getBetaShare());
        });
        svnShares.forEach(x-> {
            if (this.svnMap.get(x.getId()) == null) this.svnMap.put(x.getId(), new ArrayList<>());
            this.svnMap.get(x.getId()).add(x.getSuvShare());
        });
    }

    public BigInteger outputZ() {
        BigInteger sigmaX_u = BigInteger.ZERO;
        for (var x : y_uList) {
            sigmaX_u = sigmaX_u.add(x);
        }
        BigInteger pu = BigInteger.ZERO;
        ArrayList<Long> except = new ArrayList<>();
        for (var i : u2ids) {
            if (u3Ids.contains(i)) {
                except.add(i);
            }
        }

        for (var e : buMap.entrySet()) {
            var k = e.getKey();
            var v = e.getValue();
            LOG.info(String.valueOf(k));
            SecretShareBigInteger[] shares = new SecretShareBigInteger[v.size()];
            pu = pu.add(Shamir.combine(v.toArray(shares), order));
        }

        // Map<Long, BigInteger> sSk_uMap = new HashMap<>();
        BigInteger puv = BigInteger.ZERO;
        for (var e : svnMap.entrySet()) {
            long u = e.getKey();
            SecretShareBigInteger[] shares = new SecretShareBigInteger[e.getValue().size()];
            BigInteger key = Shamir.combine(e.getValue().toArray(shares), order);
            for (var v : except) {
                Element suv = this.sPk_uMap.get(v).getImmutable().mul(key);
                BigInteger suvBig = Utils.hash2Big(suv.toString(), order);
                LOG.debug(u + " to " + v + ": ");
                if (u > v) {
                    LOG.debug("subtract: " + suvBig);
                    puv = puv.subtract(suvBig);
                } else {
                    LOG.debug("add: " + suvBig);
                    puv = puv.add(suvBig);
                }
            }
        }
        sigmaX_u = sigmaX_u.subtract(pu).subtract(puv);
        return sigmaX_u;
    }
}
