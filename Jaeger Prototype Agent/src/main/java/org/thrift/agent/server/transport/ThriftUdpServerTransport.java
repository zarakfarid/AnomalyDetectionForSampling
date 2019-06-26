package org.thrift.agent.server.transport;

import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class ThriftUdpServerTransport extends TServerTransport {
  private ThriftUdpTransport transport;

  public ThriftUdpServerTransport(String host, int localPort) throws SocketException, UnknownHostException {
    transport = ThriftUdpTransport.newThriftUdpServer(host, localPort);
  }

  public int getPort() {
    return transport.getPort();
  }

  @Override
  public void listen() throws TTransportException {}

  @Override
  public void close() {
    if (transport.isOpen()) {
      transport.close();
    }
  }

  @Override
  protected TTransport acceptImpl() throws TTransportException {
    return transport;
  }

  @Override
  public void interrupt() {
    this.close();
  }
}
