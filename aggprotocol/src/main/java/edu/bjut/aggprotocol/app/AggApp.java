package edu.bjut.aggprotocol.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import edu.bjut.common.util.Params;
import edu.bjut.aggprotocol.entity.ParameterServer;
import edu.bjut.aggprotocol.entity.Participant;

public class AggApp {
    
    static final Logger LOG = LoggerFactory.getLogger(AggApp.class);

    public static void main(String args[]) throws IOException {
        // system setup
        var parameterServer = new ParameterServer();
        List<Participant> participants = new ArrayList<>();
        var ps = parameterServer.getParamsECC();
        for (int i = 0; i < Params.PARTICIPANT_NUM; ++i) {
            participants.add(new Participant(ps));
        }
        ImproveAggregation improveAggregation = new ImproveAggregation(parameterServer, participants);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // registration phrase
        improveAggregation.registrationPhase();
        // data aggregation
        improveAggregation.dataAggregation(0);
        stopWatch.stop();
        LOG.warn("" + stopWatch.getLastTaskTimeMillis());
    }
}
