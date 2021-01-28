package edu.bjut.verifynet.app;

import java.util.ArrayList;

import edu.bjut.common.util.Params;
import edu.bjut.verifynet.entity.Server;
import edu.bjut.verifynet.entity.TA;
import edu.bjut.verifynet.entity.User;
import edu.bjut.verifynet.message.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

public class App {
    static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
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
        Params.PARTICIPANT_NUM = userNum;
        // construct system
        TA ta = new TA();
        Server server = new Server();
        server.setParamsECC(ta.getParamsECC());
        ArrayList<User> users = new ArrayList<>();
        for (int i = 0; i < Params.PARTICIPANT_NUM ; ++i) users.add(new User(gNum));
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // round 0
        LOG.info( "Round 0: Initialization" );
        // key distribution
        for (User u: users) {
            MessageKeys msgKeys =  ta.genUserKeyPair();
            u.setKeys(msgKeys);
            u.setParamsECC(ta.getParamsECC());
        }
        // TODO u1 dropout
        for (User u: users) {
            server.appendMessagePubKey(u.getPubKeys());
        }
        if (!server.checkU1Count(Params.RECOVER_K))
            throw new RuntimeException("u1 dropout count is smaller than recovery threshold");
        // server broadcast
        server.broadcastTo(users);
        
        // round 1
        LOG.info( "Round 1: Key Sharing" );
        for (User u: users) {
            // user generate Beta and Pnm
            // ArrayList<MessagePNM> mPNMs = u.genMsgPNMs();
            ArrayList<MessageCipherPNM> mCPNMS = u.genMsgCipherPNMs();
            // send to server
            // server.appendMessagePNMs(mPNMs);
            server.appendMessageCipherPNMs(mCPNMS);
        }

        // server.broadcastToPMN(users);
        server.broadcastToCipherPMN(users);

        // round 2
        LOG.info( "Round 2: Masked Input" );

        // u2 dropout
        ArrayList<Long> dropOutUsers = new ArrayList<>();
        // int dropNum = Params.PARTICIPANT_FAILS;
        int dropNum = failNum;
        while (dropNum-- > 0) {
            LOG.debug("User nSkn:" + users.get(0).getN_sK_n());
            dropOutUsers.add(users.remove(0).getId());
        }
        LOG.info("User dropout");

        // TODO send verify additional information
        for (User u : users) {
            // user calculate gradient x_n_hat and send to server;
            MessageSigma mSigma = u.genMessageSigma();
            // send to server
            server.appendMessageSigma(mSigma);
        }
        // server broadcast id list
        server.broadcastToIds(users);

        // round 3
        LOG.info( "Round 3: Unmasking" );
        // user send dropout user shares
        for (User u : users) {
            var betaShares  = u.sendCBetaShare();
            var dropoutShares = u.sendCDropoutAndBetaShare(dropOutUsers);
            server.receiveMsgAggBeta(betaShares);
            server.receiveMsgAggDropout(dropoutShares);
        }
        // broadcast the aggregation result: Sigma x_n ...
        server.broadcastToAggResultAndProof(users);
        stopWatch.stop();
        LOG.warn("" + stopWatch.getLastTaskTimeMillis());
        // round 4
        // TODO verification
        for (User u: users) {
            if (u.verifyAggregation()) {
                LOG.info(u.getId() + ": verify success");
            } else {
                LOG.info(u.getId() + ": verify fail");
            }
        }
        
    }
}
