package edu.bjut.aggprotocol.app;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.bjut.aggprotocol.entity.ParameterServer;
import edu.bjut.aggprotocol.entity.Participant;
import edu.bjut.aggprotocol.messages.RegBack;
import edu.bjut.aggprotocol.messages.RegBack2;
import edu.bjut.aggprotocol.messages.RegBack3;
import edu.bjut.aggprotocol.messages.RegMessage2;
import edu.bjut.aggprotocol.messages.RegMessage3;
import edu.bjut.aggprotocol.messages.RepKeys;
import edu.bjut.aggprotocol.messages.RepMessage;
import edu.bjut.common.util.Utils;

public class ImproveAggregation {

    private static final Logger LOG = LoggerFactory.getLogger(ImproveAggregation.class);

    private ParameterServer parameterServer;
    private List<Participant> participants;

    public ImproveAggregation(ParameterServer parameterServer, List<Participant> participants) {
        this.parameterServer = parameterServer;
        this.participants = participants;
    }

    public boolean registrationPhase() {
        LOG.info("Start registration phase");
        // send id_i, Q_i, R_i to server
        for (var p : participants) {
            var msg = p.genRegMesssage();
            // collect Q_i, R_i
            this.parameterServer.getRegMessage(msg);
        }

        for (int i = 0; i < this.participants.size(); i++) {
            // server response R_{i-1}, R{i + 1}, all Q_i to participant
            RegBack back = parameterServer.genRegBack(i);
            // participant send X_i
            RegMessage2 reg2 = this.participants.get(i).getRegBack(back);
            // collect X_i
            parameterServer.getRegMessage2(reg2);
        }

        for (int i = 0; i < this.participants.size(); i++) {
            // server response T_i
            RegBack2 back2 = parameterServer.genRegBack2(i);
            // generate ki shares
            RegMessage3 reg3 = this.participants.get(i).getRegBack2(back2);
            // collect all shares
            parameterServer.getRegMessage3(reg3);
        }
        // distribute shares to all participant
        for (int i = 0; i < this.participants.size(); ++i) {
            RegBack3 back3 = this.parameterServer.genRegBack3(i);
            this.participants.get(i).getRegBack3(back3);
        }
        return true;
    }

    public void dataAggregation(int failNum) throws IOException {
        LOG.info("Start data aggregation.");
        boolean[] fails = Utils.setFailedParticipants(failNum, this.participants.size());
        this.parameterServer.settingFails(fails, failNum);
        RepMessage rep = null;
        // participant reports their data to the parameter server. 
        for (int i = 0; i < this.participants.size(); i++) {
            if (!fails[i]) {
                RepMessage repMessage = participants.get(i).genRepMessage();
                rep = parameterServer.getRepMessage(repMessage);
            }
        }

        // generates keys for participants
        if (failNum > 0) {
            for (int i = 0; i < this.participants.size(); i++) {
                if (!fails[i]) {
                    RepKeys repKeys = this.participants.get(i).genRepKeys(fails, failNum);
                    rep = parameterServer.getRepKeys(repKeys);
                    if (null != rep)
                        break;
                }
            }
        }

        // sends the recovered data back to participants
        for (int i = 0; i < this.participants.size(); ++i) {
            if (!fails[i]) {
                this.participants.get(i).getRepMessageFails(rep, failNum);
            }
        }
    }

}
