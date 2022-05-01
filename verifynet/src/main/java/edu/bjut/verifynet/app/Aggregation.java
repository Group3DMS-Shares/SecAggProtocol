package edu.bjut.verifynet.app;

import java.util.ArrayList;
import java.util.Map;


import edu.bjut.verifynet.entity.ParameterServer;
import edu.bjut.verifynet.entity.Participant;
import edu.bjut.verifynet.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.List;


import edu.bjut.common.big.BigVec;

import it.unisa.dia.gas.jpbc.Element;

public class Aggregation {

    private final Logger LOG = LoggerFactory.getLogger(Aggregation.class);

    private final ParameterServer parameterServer;
    private final ArrayList<Participant> participants;
    private int failNum = 0;

    public Aggregation(ParameterServer parameterServer, ArrayList<Participant> participants) {
        this.parameterServer = parameterServer;
        this.participants = participants;
    }

    public Aggregation(ParameterServer parameterServer, ArrayList<Participant> participants, int failNum) {
        this.parameterServer = parameterServer;
        this.participants = participants;
        this.failNum = failNum;
    }

    public void distributeSignPubKeys() {
        Map<Long, Element> keyMaps = new HashMap<>();
        // collection sign public keys
        participants.forEach(x -> keyMaps.put(x.getId(), x.getDuPk()));
        // distribute to every one
        participants.forEach(x -> x.setSignPubKeys(keyMaps));
    }

    public MsgResponseRound0 advertiseKeys() {
        for (var p : participants) {
            MsgRound0 msgRound0 = p.sendMsgRound0();
            this.parameterServer.recvMsgRound0(msgRound0);
        }
        return this.parameterServer.sendMsgResponseRound0();
    }

    public MsgResponseRound1 shareKeys(MsgResponseRound0 msgResponse) {
        for (var p : this.participants) {
            MsgRound1 msgRound1 = p.sendMsgRound1(msgResponse);
            this.parameterServer.recvMsgRound1(msgRound1);
        }
        return this.parameterServer.sendMsgResponseRound1();
    }

    public MsgResponseRound2 maskedInputCollection(MsgResponseRound1 msgResponse1) {
        for (int i = 0; i < this.participants.size() - this.failNum; ++i) {
            MsgRound2 msgRound2 = this.participants.get(i).sendMsgRound2(msgResponse1);
            this.parameterServer.recvMsgRound2(msgRound2);
        }
        return this.parameterServer.sendMsgResponseRound2();
    }

    public MsgResponseRound3 consistencyCheck(MsgResponseRound2 msgResponse2) {
        for (var p : this.participants) {
            MsgRound3 msgRound3 = p.sendMsgRound3(msgResponse2);
            this.parameterServer.recvMsgRound3(msgRound3);
        }
        return this.parameterServer.sendMsgResponseRound3();
    }

    public BigVec unmasking(MsgResponseRound3 msgResponse3) {
        for (var p : this.participants) {
            MsgRound4 msgRound4 = p.sendMsgRound4(msgResponse3);
            this.parameterServer.recvMsgRound4(msgRound4);
        }

        return this.parameterServer.outputZ();
    }

    void allStatics() {
        long serverTotal = timeStatics(this.parameterServer.getStopWatch());
        List<Long> clientElapses = new ArrayList<>();
        for (int i = 0; i < this.participants.size() - this.failNum; ++i) {
            clientElapses.add(timeStatics(this.participants.get(i).getStopWatch()));
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
            headBuilder.append(taskInfos[i].getTaskName());
            if (i != len - 1) {
                headBuilder.append(",");
            }
        }
        StringBuilder timeBuilder = new StringBuilder();
        for (int i = 0; i < len; ++i) {
            timeBuilder.append(taskInfos[i].getTimeMillis());
            if (i != len - 1) {
                timeBuilder.append(",");
            }
        }
        LOG.warn(headBuilder.toString());
        LOG.warn(timeBuilder.toString());
        return stopWatch.getTotalTimeMillis();
    }
}
