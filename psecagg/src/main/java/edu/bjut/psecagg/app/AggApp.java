package edu.bjut.psecagg.app;

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.bjut.psecagg.entity.ParameterServer;
import edu.bjut.psecagg.entity.Participant;
import edu.bjut.psecagg.messages.ParamsECC;
import edu.bjut.common.util.Params;

public class AggApp {

    static final Logger LOG = LoggerFactory.getLogger(AggApp.class);

    public static void main(String args[]) throws IOException {

        LOG.info("Start sec agg protocol");
        // Setup system
        ParameterServer parameterServer= new ParameterServer();
        ArrayList<Participant> participants = new ArrayList<>();
        ParamsECC paramsECC = parameterServer.getParamsECC();
        for (int i = 0; i < Params.PARTICIPANT_NUM; i++) {
            // generate secret key and public key for each user
            participants.add(new Participant(paramsECC));
        }
        Aggregation aggregation = new Aggregation(parameterServer, participants);

        // Distribute sign public keys to all user
        aggregation.distributeSignPubKeys();

        // Round 0 (AdvertiseKeys)
        aggregation.advertiseKeys();
    }
}
