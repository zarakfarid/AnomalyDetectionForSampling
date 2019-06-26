package org.thrift.agent.server;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.thrift.agent.server.handler.InMemorySpanServerHandler;
import org.thrift.agent.server.transport.ThriftUdpServerTransport;

import io.jaegertracing.agent.thrift.Agent;
import io.jaegertracing.thriftjava.Batch;

public class AgentUdpServer extends Thread {

	TServer server;
	InMemorySpanServerHandler handler;
	ThriftUdpServerTransport transport;

	public AgentUdpServer(String host, int port) throws SocketException, UnknownHostException {
		InMemorySpanServerHandler handler = new InMemorySpanServerHandler();
		ThriftUdpServerTransport transport = new ThriftUdpServerTransport(host, port);
		server =
				new TSimpleServer(
						new TServer.Args(transport)
						.protocolFactory(new TCompactProtocol.Factory())
						.processor(new Agent.Processor<InMemorySpanServerHandler>(handler)));
	}

	public int getPort() {
		return transport.getPort();
	}

	public void run() {
		server.serve();
	}

	public void close() {
		server.stop();
	}

	public Batch getBatch(int expectedSpans, int timeout) throws Exception {
		Batch batch = new Batch().setSpans(Collections.<io.jaegertracing.thriftjava.Span>emptyList());
		long expire = timeout + System.currentTimeMillis();
		while (System.currentTimeMillis() < expire) {
			Batch receivedBatch = handler.getBatch();
			if (receivedBatch.getSpans() != null) {
				batch.getSpans().addAll(receivedBatch.getSpans());
				batch.setProcess(receivedBatch.getProcess());
			}

			if (batch.spans.size() >= expectedSpans) {
				return batch;
			}

			Thread.sleep(1);
		}

		return batch;
	}

}
