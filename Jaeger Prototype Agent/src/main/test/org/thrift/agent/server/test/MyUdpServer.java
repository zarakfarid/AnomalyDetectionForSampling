package org.thrift.agent.server.test;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TCompactProtocol.Factory;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TProtocolUtil;
import org.thrift.agent.server.transport.ThriftUdpTransport;

import io.jaegertracing.thriftjava.Batch;
import lombok.ToString;

public class MyUdpServer {

	public static final String DEFAULT_AGENT_UDP_HOST = "localhost";
	public static final int DEFAULT_AGENT_UDP_COMPACT_PORT = 6831;

	@ToString.Exclude private static ThriftUdpTransport udpTransport;

	public static void StartsimpleServer() {
		try {
			System.out.println("Starting the UDP Server");
			udpTransport = ThriftUdpTransport.newThriftUdpServer("localhost", 6831);
			byte[] bytes = new byte[65000];
			TDeserializer deserializer = new TDeserializer(new TCompactProtocol.Factory());
			Batch batch = new Batch();

			while(true) {
				int size = udpTransport.read(bytes, 0, 65000);
				String s = new String(bytes);
				System.out.println("Size:"+size);

				TProtocolFactory fallback = null;
				TProtocolFactory i = TProtocolUtil.guessProtocolFactory(bytes, fallback);
				System.out.println("Shits Compact:"+i.getClass().getName());
				
				Factory x  = new TCompactProtocol.Factory();
				try {
					if(size > 0) {
						System.out.println("Read something from UDP Port");
						System.out.println(s);
						x.getProtocol(udpTransport).getTransport().write(bytes);
						
						deserializer.deserialize(batch, bytes);
						System.out.println("SERVICE NAME:"+batch.getProcess().getServiceName());
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
				bytes = new byte[65000];
			}		    
			// Use this for a multithreaded server
			// TServer server = new TThreadPoolServer(new
			// TThreadPoolServer.Args(serverTransport).processor(processor));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		StartsimpleServer();
	}

}