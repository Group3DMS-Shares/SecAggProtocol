package edu.bjut.psecagg.app;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import edu.bjut.psecagg.entity.ParameterServer;
import edu.bjut.psecagg.entity.Participant;
import edu.bjut.psecagg.messages.MsgRound0;
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
        participants.forEach(x->keyMaps.put(x.getId(), x.getDuPk()));
        // distribute to every one
        participants.forEach(x-> x.setSignPubKeys(keyMaps));
    }

    public void advertiseKeys() {
        for (var p : participants) {
            MsgRound0 msgRound0 =   p.sendMsgRound0();
            this.parameterServer.recvMsgRound0(msgRound0);

        }
    }


}
