package edu.bjut.psecagg.entity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.bjut.common.shamir.SecretShareBigInteger;
import edu.bjut.common.shamir.Shamir;
import edu.bjut.common.big.BigVec;
import edu.bjut.common.messages.ParamsECC;
import edu.bjut.common.util.PRG;
import edu.bjut.common.util.Utils;
import edu.bjut.psecagg.messages.*;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

public class ParameterServer {

    static final Logger LOG = LoggerFactory.getLogger(ParameterServer.class);
    private final int userNum;
    private final int recoverThreshold;

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
    private ArrayList<BigVec> y_uList = new ArrayList<>();

    // Round 3
    private int u4Count = 0;
    private ArrayList<MsgRound3> u4Sigmas = new ArrayList<>();

    // Round 4
    Map<Long, ArrayList<SecretShareBigInteger>> buMap = new HashMap<>();
    Map<Long, ArrayList<SecretShareBigInteger>> svnMap = new HashMap<>();
    // Time statistic
    private StopWatch stopWatch = new StopWatch("server");

    public ParameterServer(int userNum) {
        this.pairing = PairingFactory.getPairing("aggVote1.properties");
        this.order = pairing.getG1().getOrder();
        this.g = this.pairing.getG1().newRandomElement().getImmutable();
        this.userNum = userNum;
        this.recoverThreshold= userNum / 2 + 1;
    }

    public StopWatch getStopWatch() {
        return this.stopWatch;
    }

    public ParamsECC getParamsECC() {
        ParamsECC ps = new ParamsECC(this.pairing, this.g);
        return ps;
    }

    public void recvMsgRound0(MsgRound0 msgRound0) {
        this.stopWatch.start("round0_receive");
        msgRound0s.add(msgRound0);
        this.sPk_uMap.put(msgRound0.getId(), msgRound0.getsPk_u());
        this.cPk_uMap.put(msgRound0.getId(), msgRound0.getcPk_u());
        this.stopWatch.stop();
    }

    public MsgResponseRound0 sendMsgResponseRound0() {
        this.u1Count = msgRound0s.size();
        assert (this.u1Count >= this.recoverThreshold);
        return new MsgResponseRound0(msgRound0s);
    }

    public void recvMsgRound1(MsgRound1 msgRound1) {
        this.stopWatch.start("round1_receive");
        this.allMsgRound1s.add(msgRound1);
        this.u2ids.add(msgRound1.getId());
        this.stopWatch.stop();
    }

    public MsgResponseRound1 sendMsgResponseRound1() {
        this.u2Count = this.allMsgRound1s.size();
        assert (this.u2Count >= this.recoverThreshold);
        return new MsgResponseRound1(this.allMsgRound1s);
    }

    public void recvMsgRound2(MsgRound2 msgRound2) {
        this.u3Ids.add(msgRound2.getId());
        this.y_uList.add(msgRound2.getY());
    }

    public MsgResponseRound2 sendMsgResponseRound2() {
        this.u3Count = this.u3Ids.size();
        assert (this.u3Count >= this.recoverThreshold);
        return new MsgResponseRound2(u3Ids);
    }

    public void recvMsgRound3(MsgRound3 msgRound3) {
        this.stopWatch.start("round3_send");
        this.u4Sigmas.add(msgRound3);
        this.stopWatch.stop();
    }

    public MsgResponseRound3 sendMsgResponseRound3() {
        this.u4Count = this.u4Sigmas.size();
        assert (this.u4Count >= this.recoverThreshold);
        return new MsgResponseRound3(this.u4Sigmas);
    }

    public void recvMsgRound4(MsgRound4 msgRound4) {
        this.stopWatch.start("round4_recive");
        var betaShares = msgRound4.getBetaShares();
        var svnShares = msgRound4.getSvuShares();
        betaShares.forEach(x -> {
            this.buMap.computeIfAbsent(x.getId(), k -> new ArrayList<>());
            this.buMap.get(x.getId()).add(x.getBetaShare());
        });
        svnShares.forEach(x -> {
            this.svnMap.computeIfAbsent(x.getId(), k -> new ArrayList<>());
            this.svnMap.get(x.getId()).add(x.getUShare());
        });
        this.stopWatch.stop();
    }

    public BigVec outputZ() {
        this.stopWatch.start("agg_1");
        int gSize = this.y_uList.get(0).size();
        BigVec sigmaX_u = BigVec.Zero(gSize);
        for (var x : y_uList) {
            sigmaX_u = sigmaX_u.add(x);
        }
        BigVec pu = BigVec.Zero(gSize);

        ArrayList<Long> except = new ArrayList<>();
        for (var i : u2ids) {
            if (u3Ids.contains(i)) {
                except.add(i);
            }
        }
        this.stopWatch.stop();
        this.stopWatch.start("agg_2");
        for (var e : buMap.entrySet()) {
            var k = e.getKey();
            var v = e.getValue();
            LOG.info(String.valueOf(k));
            SecretShareBigInteger[] shares = new SecretShareBigInteger[v.size()];
            var puBig = Shamir.combine(v.toArray(shares), order);
            PRG prg = new PRG(puBig.toString());
            var puBigArray = prg.genBigs(gSize);
            pu = pu.add(new BigVec(puBigArray));
        }
        this.stopWatch.stop();
        this.stopWatch.start("agg_3");
        BigVec puv = BigVec.Zero(gSize);
        for (var e : svnMap.entrySet()) {
            long u = e.getKey();
            SecretShareBigInteger[] shares = new SecretShareBigInteger[e.getValue().size()];
            BigInteger key = Shamir.combine(e.getValue().toArray(shares), order);
            for (var v : except) {
                Element suv = this.sPk_uMap.get(v).getImmutable().mul(key);
                BigInteger suvBig = Utils.hash2Big(suv.toString(), order);
                LOG.debug(u + " to " + v + ": ");
                PRG prg = new PRG(suvBig.toString());
                var suvBigArray = prg.genBigs(gSize);
                if (u > v) {
                    LOG.debug("subtract: " + suvBig);
                    puv = puv.subtract(new BigVec(suvBigArray));
                } else {
                    LOG.debug("add: " + suvBig);
                    puv = puv.add(new BigVec(suvBigArray));
                }
            }
        }
        sigmaX_u = sigmaX_u.subtract(pu).subtract(puv);
        this.stopWatch.stop();
        return sigmaX_u;
    }
}
