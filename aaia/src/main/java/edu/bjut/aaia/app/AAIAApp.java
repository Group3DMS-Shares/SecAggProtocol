package edu.bjut.aaia.app;

import edu.bjut.aaia.entity.ParameterServer;
import edu.bjut.aaia.entity.Participant;
import edu.bjut.common.big.BigVec;
import edu.bjut.common.messages.ParamsECC;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.ArrayList;

public  class AAIAApp {

    static final Logger LOG = LoggerFactory.getLogger(AAIAApp.class);

    public static void main(String[] args)  {
        // args setting
        ArgumentParser parser = ArgumentParsers.newFor("Params Setting").build().defaultHelp(true)
                .description("experiment setting: user number, failure number, gradient number");
        parser.addArgument("-u", "--user").type(Integer.class).help("Specify user number");
        parser.addArgument("-f", "--failure").setDefault(0).type(Integer.class).help("Specify dropout user number");
        parser.addArgument("-g", "--gradients").setDefault(1).type(Integer.class).help("Specify gradients number");
        Namespace ns = parser.parseArgsOrFail(args);
        Integer userNum = ns.getInt("user");
        Integer failNum = ns.getInt("failure");
        Integer gNum = ns.getInt("gradients");
        if (userNum == null) {
            parser.printHelp();
            System.exit(0);
        }
        LOG.info("Start secure aggregation protocol");
        // Setup system
        ParameterServer parameterServer= new ParameterServer(userNum);
        ArrayList<Participant> participants = new ArrayList<>();
        ParamsECC paramsECC = parameterServer.getParamsECC();
        for (int i = 0; i < userNum; i++) {
            // generate secret key and public key for each user
            participants.add(new Participant(paramsECC, gNum, userNum));
        }
        Aggregation aggregation = new Aggregation(parameterServer, participants, failNum);

        // Distribute sign public keys to all user
        aggregation.distributeSignPubKeys();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // Round 0 (AdvertiseKeys)
        // var msgResponse0 = aggregation.advertiseKeys();
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
        BigVec z = aggregation.unmasking(msgResponse3);
        LOG.info("Aggregation results: " + z.toString());
        stopWatch.stop();
        aggregation.allStatics();
        LOG.warn("" + stopWatch.getLastTaskTimeMillis());
    }
}
