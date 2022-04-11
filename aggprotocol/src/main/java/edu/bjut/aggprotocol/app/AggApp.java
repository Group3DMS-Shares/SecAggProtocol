package edu.bjut.aggprotocol.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import edu.bjut.common.util.Params;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import edu.bjut.aggprotocol.entity.ParameterServer;
import edu.bjut.aggprotocol.entity.Participant;

public class AggApp {
    
    static final Logger LOG = LoggerFactory.getLogger(AggApp.class);

    public static void main(String args[]) throws IOException {
        // args setting
        ArgumentParser parser = ArgumentParsers.newFor("Params Setting").build().defaultHelp(true)
                .description("experiment setting: user number, failure number, gradient number");
        parser.addArgument("-u", "--user").type(Integer.class).help("Specify user number");
        parser.addArgument("-f", "--failure").setDefault(0).type(Integer.class).help("Specify dropout user number");
        parser.addArgument("-g", "--gradients").setDefault(1).type(Integer.class).help("Specify gradients number");
        Namespace ns = parser.parseArgsOrFail(args);
        Integer userNum = ns.getInt("user");
        Integer failNum = ns.getInt("failure");
        Params.G_SIZE = ns.getInt("gradients");
        if (userNum == null) {
            parser.printHelp();
            System.exit(0);
        }
        Params.PARTICIPANT_NUM = userNum;
        LOG.info("recovery threshold: " + Params.RECOVER_K);
        // system setup
        var parameterServer = new ParameterServer();
        List<Participant> participants = new ArrayList<>();
        var ps = parameterServer.getParamsECC();
        for (int i = 0; i < Params.PARTICIPANT_NUM; ++i) {
            participants.add(new Participant(ps));
        }
        ImproveAggregation improveAggregation = new ImproveAggregation(parameterServer, participants, failNum);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // registration phrase
        improveAggregation.registrationPhase();
        // data aggregation
        improveAggregation.dataAggregation();
        stopWatch.stop();
        improveAggregation.allStatics();
        LOG.warn(Long.toString(stopWatch.getLastTaskTimeMillis()));
    }
    
    
}
