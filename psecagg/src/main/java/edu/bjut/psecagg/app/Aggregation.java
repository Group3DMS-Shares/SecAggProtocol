package edu.bjut.psecagg.app;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import edu.bjut.psecagg.messages.MsgRound4;
import edu.bjut.common.big.BigVec;
import edu.bjut.psecagg.entity.ParameterServer;
import edu.bjut.psecagg.entity.Participant;
import edu.bjut.psecagg.messages.*;
import it.unisa.dia.gas.jpbc.Element;

public class Aggregation {

    private ParameterServer parameterServer;
    private ArrayList<Participant> participants;

    public Aggregation(ParameterServer parameterServer, ArrayList<Participant> participants) {
        this.parameterServer = parameterServer;
        this.participants = participants;

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
        MsgResponseRound0 msgResponseRound0 = this.parameterServer.sendMsgResponseRound0();
        return msgResponseRound0;
    }

    public MsgResponseRound1 shareKeys(MsgResponseRound0 msgResponse) {
        for (var p : this.participants) {
            MsgRound1 msgRound1 = p.sendMsgRound1(msgResponse);
            this.parameterServer.recvMsgRound1(msgRound1);
        }
        return this.parameterServer.sendMsgResponseRound1();
    }

    public MsgResponseRound2 maskedInputCollection(MsgResponseRound1 msgResponse1, int failNum) {
        for (int i = 0; i < this.participants.size() - failNum; ++i) {
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
}
