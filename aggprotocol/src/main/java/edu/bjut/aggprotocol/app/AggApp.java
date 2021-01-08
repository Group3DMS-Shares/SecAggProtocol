package edu.bjut.aggprotocol.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.bjut.common.util.Params;
import edu.bjut.aggprotocol.entity.ParameterServer;
import edu.bjut.aggprotocol.entity.Participant;

public class AggApp {

    public static void main(String args[]) throws IOException {
        // system setup
        var parameterServer = new ParameterServer();
        List<Participant> participants = new ArrayList<>();
        var ps = parameterServer.getParamsECC();
        for (int i = 0; i < Params.PARTICIPANT_NUM; ++i) {
            participants.add(new Participant(ps));
        }
        ImproveAggregation improveAggregation = new ImproveAggregation(parameterServer, participants);
        // registration phrase
        improveAggregation.registrationPhase();
        // data aggregation
        improveAggregation.dataAggregation(1);

    }
}
