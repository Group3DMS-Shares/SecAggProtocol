package edu.bjut.verifynet.app;

import java.util.ArrayList;

import edu.bjut.common.util.Params;
import edu.bjut.verifynet.entity.Server;
import edu.bjut.verifynet.entity.TA;
import edu.bjut.verifynet.entity.User;
import edu.bjut.verifynet.message.MessageBetaShare;
import edu.bjut.verifynet.message.MessageDroupoutShare;
import edu.bjut.verifynet.message.MessageKeys;
import edu.bjut.verifynet.message.MessagePNM;
import edu.bjut.verifynet.message.MessageSigma;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        // construct system
        TA ta = new TA();
        Server server = new Server();
        server.setParamsECC(ta.getParamsECC());
        ArrayList<User> users = new ArrayList<User>();
        for (int i = 0; i < Params.PARTICIPANT_NUM ; ++i) users.add(new User());

        // round 0
        System.out.println( "Round 0: Initialization" );
        // key distribution
        for (User u: users) {
            MessageKeys msgKeys =  ta.genUserKeyPair();
            u.setKeys(msgKeys);
            u.setParamsECC(ta.getParamsECC());
        }
        // TODO u1 droupout
        for (User u: users) {
            server.appendMessagePubkey(u.getPubKeys());
        }
        if (false == server.checkU1Count(Params.RECOVER_K))
            throw new RuntimeException("u1 droupout count is smaller than recovery threshold");
        // server broadcast
        server.broadcastTo(users);
        
        // round 1
        System.out.println( "Round 1: Key Sharing" );
        for (User u: users) {
            // user generate Beta and Pnm
            ArrayList<MessagePNM> mPnms = u.genMsgPNMs();
            // send to server
            server.appendMessagePNMs(mPnms);
        } 

        server.broadcastToPMN(users);

        // round 2
        System.out.println( "Round 2: Masked Input" );

        // u2 droupout
        ArrayList<Long> droupOutUsers = new ArrayList<>();
        int droupNum = Params.PARTICIPANT_FAILS;
        while (droupNum-- > 0) {
            System.out.println("User nSkn:" + users.get(0).getN_sK_n());
            droupOutUsers.add(users.remove(0).getId());
        }
        System.out.println("User dropout");

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
        System.out.println( "Round 3: Unmasking" );
        // user send dropout user shares
        for (User u : users) {
            ArrayList<MessageBetaShare> mBetaShares = u.sendBetaShare(); 
            ArrayList<MessageDroupoutShare> mDroupoutShares = u.sendDropoutAndBetaShare(droupOutUsers); 
            server.receiveMsgAggBeta(mBetaShares);
            server.receiveMsgAggDroupout(mDroupoutShares);
        }
        // broadcast the aggregation result: Sigma x_n ...
        server.broadcastToAggResultAndProof(users);
        // round 4
        // TDO verification
        for (User u: users) {
            if (u.verifyAggregation()) {
                System.out.println(u.getId() + ": verify success");
            } else {
                System.out.println(u.getId() + ": verify fail");
            }
        }
        
    }
}
