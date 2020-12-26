package edu.bjut.app;

import java.util.ArrayList;

import edu.bjut.entity.MessageKeys;
import edu.bjut.entity.Params;
import edu.bjut.entity.Server;
import edu.bjut.entity.TA;
import edu.bjut.entity.User;

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
        for (User u: users) {
            server.appendMessagePubkey(u.getPubKeys());
        }
        // server broadcast
        server.broadcastTo(users);
        
        // round 1
        System.out.println( "Round 1: Key Sharing" );
        // user generate Beta and Pnm
        for (User u: users) {
            server.appendMessagePNM(u.genMsgPNMs());
        } 
        server.broadcastToPMN(users);

        // round 2
        System.out.println( "Round 2: Masked Input" );
        // user caculate gradient x_n_hat and send to server;
        // TODO send verify additionnal information
        for (User u : users) {
            server.appendMessageSigma(u.genMessageSigma());
        }
        // server broadcast id list
        server.broadcastToIds(users);

        // round 3
        System.out.println( "Round 3: Unmasking" );
        // TODO process dropout users;

        // user send dropout user shares
        for (User u : users) {
            server.receiveMessageAgg(u.sendDropoutAndBeta());
        }
        // broadcast the aggregation result: Sigma x_n ...
        server.broadcastToAggResultAndProof(users);
        
    }
}
