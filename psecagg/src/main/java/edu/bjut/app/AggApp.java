package edu.bjut.app;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.bjut.entity.ParameterServer;
import edu.bjut.entity.Participant;
import edu.bjut.messages.ParamsECC;
import edu.bjut.util.Params;

public class AggApp {
	static final Logger LOG = LoggerFactory.getLogger(AggApp.class);

	private static ParameterServer parameterServer;
	private static Participant[] participant;


	public static void main(String args[]) throws IOException {

		LOG.info("Start sec agg protocol");
		// aggPhaseVaryingMeterNumber();
		// aggWithFails();
		psecAgg();
	}

	private static void psecAgg() throws IOException {
		// setup
		parameterServer = new ParameterServer();
		for (int num : Params.ARRAY_OF_PARTICIPANT_NUM) {
			Params.PARTICIPANT_NUM = num;
			parameterServer = new ParameterServer();
			ParamsECC paramsECC = parameterServer.getParamsECC();
			participantIntialiaztion(paramsECC);
		}
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
}
