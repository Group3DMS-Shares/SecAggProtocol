package edu.bjut.psecagg.app;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import edu.bjut.common.messages.ParamsECC;
import edu.bjut.common.util.Params;
import edu.bjut.psecagg.entity.ParameterServer;
import edu.bjut.psecagg.entity.Participant;

public class AggApp {

    static final Logger LOG = LoggerFactory.getLogger(AggApp.class);

    public static void main(String args[]) throws IOException {

        LOG.info("Start secure aggregation protocol");
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
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // Round 0 (AdvertiseKeys)
        var msgResponse0 = aggregation.advertiseKeys();
        if (null == msgResponse0) throw new RuntimeException("smaller than share threshold");

        // Round1 (ShareKeys)
        var msgResponse1 =  aggregation.shareKeys(msgResponse0);
        if (null == msgResponse1) throw new RuntimeException("smaller than share threshold");

        // Round 2
        var msgResponse2 = aggregation.maskedInputCollection(msgResponse1);
        if (null == msgResponse2) throw new RuntimeException("smaller than share threshold");

        // Round 3
        var msgResponse3 = aggregation.consistencyCheck(msgResponse2);
        if (null == msgResponse3) throw new RuntimeException("smaller than share threshold");

        // Round 4
        BigInteger z = aggregation.unmasking(msgResponse3);
        LOG.info("Aggregation results: " + z.toString());
        stopWatch.stop();
        LOG.warn("" + stopWatch.getLastTaskTimeMillis());
    }
}
