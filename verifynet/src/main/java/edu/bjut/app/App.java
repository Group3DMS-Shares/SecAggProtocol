package edu.bjut.app;

import java.math.BigInteger;
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
        
        System.out.println( "Round 1: Key Sharing" );
        // generate Beta and Pnm
        for (User u: users) {
            server.appendMessagePNM(u.genMsgPNMs());
        } 
        server.recoverSecret(0);
        // server.broadcastToPMN(users);
        BigInteger result = BigInteger.ZERO;
        for (User u: users) {
           result = result.add(u.genEncGradient());
        }
        System.out.println(result);
    }
}
