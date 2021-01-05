package edu.bjut.aggprotocol.app;

import java.io.IOException;

import edu.bjut.common.messages.ParamsECC;
import edu.bjut.common.util.Out;
import edu.bjut.common.util.Params;
import edu.bjut.common.util.Utils;
import edu.bjut.aggprotocol.entity.ParameterServer;
import edu.bjut.aggprotocol.entity.Participant;
import edu.bjut.aggprotocol.messages.RegBack;
import edu.bjut.aggprotocol.messages.RegBack2;
import edu.bjut.aggprotocol.messages.RegBack3;
import edu.bjut.aggprotocol.messages.RegMessage;
import edu.bjut.aggprotocol.messages.RegMessage2;
import edu.bjut.aggprotocol.messages.RegMessage3;
import edu.bjut.aggprotocol.messages.RepKeys;
import edu.bjut.aggprotocol.messages.RepMessage;

public class Main {
    private static Out out;

    private static ParameterServer parameterServer;
    private static Participant[] participant;


    public static void main(String args[]) throws IOException {

        // aggPhaseVaryingMeterNumber();
        aggWithFails();

        out.close();
        Runtime.getRuntime().exec("shutdown -s");
    }

    public static void aggPhaseVaryingMeterNumber() throws IOException {

        printAndWrite("meter number meter number meter number");

        parameterServer = new ParameterServer();
        for (int num : Params.ARRAY_OF_PARTICIPANT_NUM) {
            Params.PARTICIPANT_NUM = num;
            parameterServer = new ParameterServer();
            ParamsECC ps = parameterServer.getParamsECC();
            participantIntialiaztion(ps);

            double totalTime = 0;
            for (int j = 0; j < Params.EXPERIMENT_REPEART_TIMES; j++) {
                totalTime += oneTimeRegTime();
                clear();
            }
            printAndWrite("reg reg reg with meter number : " + num);
            printAndWriteData(totalTime / Params.EXPERIMENT_REPEART_TIMES);

            oneTimeRegTime();
            totalTime = 0;
            for (int j = 0; j < Params.EXPERIMENT_REPEART_TIMES; j++) {
                totalTime += oneTimeMeterRepTime();
            }
            printAndWrite("rep rep rep with meter number : " + num);
            printAndWriteData(totalTime / Params.EXPERIMENT_REPEART_TIMES);
        }
    }

    private static void aggWithFails() throws IOException {
        printAndWrite("meter number meter number meter number");

        for (int fail : Params.ARRAY_OF_PARTICIPANT_FAILS) {

            Params.PARTICIPANT_FAILS = fail;
            double totalTime = 0;

            parameterServer = new ParameterServer();
            ParamsECC ps = parameterServer.getParamsECC();
            participantIntialiaztion(ps);
            oneTimeRegTime();

            Params.fails = Utils.setFailedParticipants(fail);
            for (int j = 0; j < Params.EXPERIMENT_REPEART_TIMES; j++) {
                totalTime += oneRepTimeWithFailedParticipant(fail);
            }
            printAndWrite("rep fail rep fail rep fail with meter number : " + fail);
            printAndWriteData(totalTime / Params.EXPERIMENT_REPEART_TIMES);
        }


    }

    private static void printAndWriteData(double totalTime) {
        System.out.println(totalTime / 1000000);
        out.println(totalTime / 1000000);
        printAndWrite("");
        printAndWrite("");
    }

    private static void printAndWrite(String outStr) {
        System.out.println(outStr);
        out.println(outStr);
    }

    private static void participantIntialiaztion(ParamsECC ps) throws IOException {
        participant = new Participant[Params.PARTICIPANT_NUM];

        for (int i = 0; i < participant.length; i++) {
            participant[i] = new Participant(ps, i);
        }
    }

    private static void clear() throws IOException {
        parameterServer.clear();
    }

    private static long oneTimeRegTime() throws IOException {
        long sl = System.nanoTime();
        for (int i = 0; i < participant.length; i++) {
            RegMessage reg = participant[i].genRegMesssage();
            parameterServer.getRegMessage(reg);
        }

        for (int i = 0; i < participant.length; i++) {
            RegBack back = parameterServer.genRegBack(i);
            RegMessage2 reg2 = participant[i].getRegBack(back);
            parameterServer.getRegMessage2(reg2);
        }

        for (int i = 0; i < participant.length; i++) {
            RegBack2 back2 = parameterServer.genRegBack2(i);

            RegMessage3 reg3 = participant[i].getRegBack2(back2);
            parameterServer.getRegMessage3(reg3);
        }

        for (int i = 0; i < participant.length; i++) {
            RegBack3 back3 = parameterServer.genRegBack3(i);
            participant[i].getRegBack3(back3);
        }

        long el = System.nanoTime();
        return (el - sl);
    }

    private static long oneTimeMeterRepTime() throws IOException {
        long sl = System.nanoTime();
        RepMessage rep = null;
        for (int i = 0; i < Params.PARTICIPANT_NUM; i++) {
            RepMessage repMessage = participant[i].genRepMessage();
            rep = parameterServer.getRepMessage(repMessage);
        }
        for (int i = 0; i < Params.PARTICIPANT_NUM; i++) {
            participant[i].getRepMessage(rep);
        }
        System.out.println();
        long el = System.nanoTime();
        return (el - sl);
    }

    private static long oneRepTimeWithFailedParticipant(int num) throws IOException {
        long sl = System.nanoTime();
        RepMessage rep = null;

        // participant reports their data to the parameter server. 
        for (int i = 0; i < Params.PARTICIPANT_NUM; i++) {
            if (Params.fails[i] == 1) {
                // RepMessage repMessage = participant[i].genRepMessage();
                continue;
            }
            RepMessage repMessage = participant[i].genRepMessage();
            rep = parameterServer.getRepMessage(repMessage);
        }

        //generates keys for participants
        for (int i = 0; i < Params.PARTICIPANT_NUM; i++) {
            if (Params.fails[i] == 1)
                continue;

            RepKeys repKeys = participant[i].genRepKeys(Params.fails, num);
            rep = parameterServer.getRepKeys(repKeys);

            if (null != rep)
                break;
        }

        //sends the recovered data back to participants
        for (int i = 0; i < Params.PARTICIPANT_NUM; i++) {
            if (Params.fails[i] == 1)
                continue;
            participant[i].getRepMessageFails(rep);
        }
        System.out.println();

        long el = System.nanoTime();
        return (el - sl);
    }

}
