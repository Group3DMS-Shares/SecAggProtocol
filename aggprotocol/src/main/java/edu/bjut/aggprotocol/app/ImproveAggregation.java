package edu.bjut.aggprotocol.app;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import edu.bjut.aggprotocol.entity.ParameterServer;
import edu.bjut.aggprotocol.entity.Participant;
import edu.bjut.aggprotocol.messages.RegBack;
import edu.bjut.aggprotocol.messages.RegBack2;
import edu.bjut.aggprotocol.messages.RegBack3;
import edu.bjut.aggprotocol.messages.RegMessage2;
import edu.bjut.aggprotocol.messages.RegMessage3;
import edu.bjut.aggprotocol.messages.RepKeys;
import edu.bjut.aggprotocol.messages.RepMessage;
import edu.bjut.common.shamir.SecretShareBigInteger;
import edu.bjut.common.util.Utils;

public class ImproveAggregation {

    private static final Logger LOG = LoggerFactory.getLogger(ImproveAggregation.class);

    private ParameterServer parameterServer;
    private List<Participant> participants;
    private boolean[] fails; 
    private int failNum;

    public ImproveAggregation(ParameterServer parameterServer, List<Participant> participants, int failNum) {
        this.parameterServer = parameterServer;
        this.participants = participants;
        this.failNum = failNum;
        this.fails = Utils.setFailedParticipants(failNum, this.participants.size());
        this.parameterServer.settingFails(this.fails, this.failNum);
    }

    public boolean registrationPhase() {
        LOG.info("Start registration phase");
        // send id_i, Q_i, R_i to server
        for (var p : participants) {
            var msg = p.genRegMesssage();
            // collect Q_i, R_i
            this.parameterServer.getRegMessage(msg);
        }

        for (int i = 0; i < this.participants.size(); i++) {
            // server response R_{i-1}, R{i + 1}, all Q_i to participant
            RegBack back = parameterServer.genRegBack(i);
            // participant send X_i
            RegMessage2 reg2 = this.participants.get(i).getRegBack(back);
            // collect X_i
            parameterServer.getRegMessage2(reg2);
        }

        for (int i = 0; i < this.participants.size(); i++) {
            // server response T_i
            RegBack2 back2 = this.parameterServer.genRegBack2(i);
            // generate ki shares
            RegMessage3 reg3 = this.participants.get(i).getRegBack2(back2);
            // collect all shares
            this.parameterServer.getRegMessage3(reg3);
        }
        // distribute shares to all participant
        for (int i = 0; i < this.participants.size(); ++i) {
            RegBack3 back3 = this.parameterServer.genRegBack3(i);
            this.participants.get(i).getRegBack3(back3);
        }
        return true;
    }

    public void dataAggregation() {
        LOG.info("Start data aggregation.");


        LOG.info("Gen share bu");
        for (int i = 0; i < this.participants.size(); i++) {
            var msg = this.participants.get(i).genBuMsg();
            this.parameterServer.getBuShares(msg);
        }

        LOG.info("Distribute share bu");
        for (int i = 0; i  < this.participants.size(); i++) {
            var msg = this.parameterServer.genBuShares(i);
            this.participants.get(i).collectBuShares(msg);
        }
        RepMessage rep = null;
        // participant reports their data to the parameter server. 
        for (int i = 0; i < this.participants.size(); i++) {
            if (!this.fails[i]) {
                RepMessage repMessage = this.participants.get(i).genRepMessage();
                rep = this.parameterServer.getRepMessage(repMessage);
            }
        }

        // generates keys for participants
        if (failNum > 0) {
            for (int i = 0; i < this.participants.size(); i++) {
                if (!this.fails[i]) {
                    RepKeys repKeys = this.participants.get(i).genRepKeys(fails, failNum);
                    rep = parameterServer.getRepKeys(repKeys);
                    if (null != rep)
                        break;
                }
            }
        }
        LOG.info("Collect share bu");
        for (int i = 0; i < this.participants.size(); ++i) {
            SecretShareBigInteger[] msgBuShares = this.participants.get(i).sendBuShares(this.fails);
            this.parameterServer.collectBuShares(i, msgBuShares);
        }
        this.parameterServer.aggregation();
        if (rep == null) {
            LOG.error("the num of user is smaller than recover threshold");
            return;
        }
        rep.setCi(this.parameterServer.aggResult);
        // sends the recovered data back to participants
        for (int i = 0; i < this.participants.size(); ++i) {
            if (!this.fails[i]) {
                this.participants.get(i).getRepMessageFails(rep, failNum);
            }
        }
    }

    void allStatics() {
        long serverTotal = timeStatics(this.parameterServer.getStopWatch());
        List<Long> clientElapses = new ArrayList<>();
        for (int i = 0; i < this.participants.size(); ++i) {
            if (!fails[i]) {
                clientElapses.add(timeStatics(this.participants.get(i).getStopWatch()));
            }
        }
        long clientTotal = 0;
        for (var i : clientElapses) {
            LOG.warn("client:" + i);
            clientTotal += i;
        }
        LOG.warn("server:" + serverTotal);
        LOG.warn("total: " + (serverTotal + clientTotal));
    }

    private long timeStatics(StopWatch stopWatch) {
        var taskInfos = stopWatch.getTaskInfo();
        int len = taskInfos.length;
        StringBuilder headBuilder = new StringBuilder();
        for (int i = 0; i < len; ++i) {
            headBuilder = headBuilder.append(taskInfos[i].getTaskName());
            if (i != len - 1) {
                headBuilder = headBuilder.append(",");
            }
        }
        StringBuilder timeBuilder = new StringBuilder();
        for (int i = 0; i < len; ++i) {
            timeBuilder = timeBuilder.append(taskInfos[i].getTimeMillis());
            if (i != len - 1) {
                timeBuilder = timeBuilder.append(",");
            }
        }
        LOG.warn(headBuilder.toString());
        LOG.warn(timeBuilder.toString());
        return stopWatch.getTotalTimeMillis();
    }

}
