package edu.bjut.app;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import edu.bjut.entity.ParameterServer;
import edu.bjut.entity.Participant;
import edu.bjut.messages.MsgRound0;
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
        participants.forEach(x->keyMaps.put(x.getId(), x.getQi()));
        // distribute to every one
        participants.forEach(x-> x.setSignPubkeys(keyMaps));
    }

    public void advertiseKeys() {
        for (var p : participants) {
            MsgRound0 msgRound0 =   p.sendMsgRound0();
        }
    }


}
