package org.thrift.agent.app;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.thrift.agent.filter.AgentMLFilter;
import org.thrift.agent.server.AgentUdpServer;

public class Application {

	private static Logger logger = LogManager.getLogger(Application.class);

	public static void main(String[] args) {
		try {
			AgentUdpServer server = new AgentUdpServer("localhost", 6831);
			server.run();

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
