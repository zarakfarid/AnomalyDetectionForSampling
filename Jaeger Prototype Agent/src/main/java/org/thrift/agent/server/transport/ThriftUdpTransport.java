package org.thrift.agent.server.transport;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import lombok.ToString;

public class ThriftUdpTransport extends TTransport implements Closeable {
	public static final int MAX_PACKET_SIZE = 65000;

	public int receiveOffSet = -1;
	public int receiveLength = 0;

	@ToString.Exclude public final DatagramSocket socket;
	@ToString.Exclude public byte[] receiveBuf;
	@ToString.Exclude public ByteBuffer writeBuffer;

	// Create a UDP server for receiving data on specific host and port
	public static ThriftUdpTransport newThriftUdpServer(String host, int port)
			throws SocketException, UnknownHostException {
		ThriftUdpTransport t = new ThriftUdpTransport();
		t.socket.bind(new InetSocketAddress(host, port));
		return t;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private ThriftUdpTransport() throws SocketException {
		this.socket = new DatagramSocket(null);
	}

	int getPort() {
		return socket.getLocalPort();
	}

	@Override
	public boolean isOpen() {
		return !this.socket.isClosed();
	}

	// noop as opened in constructor
	@Override
	public void open() throws TTransportException {}

	// close underlying socket
	@Override
	public void close() {
		this.socket.close();
	}

	@Override
	public int read(byte[] bytes, int offset, int len) throws TTransportException {
		if (!this.isOpen()) {
			throw new TTransportException(TTransportException.NOT_OPEN);
		}
		if (this.receiveOffSet == -1) {
			this.receiveBuf = new byte[MAX_PACKET_SIZE];
			DatagramPacket dg = new DatagramPacket(this.receiveBuf, MAX_PACKET_SIZE);
			try {
				this.socket.receive(dg);
			} catch (IOException e) {
				throw new TTransportException(
						TTransportException.UNKNOWN, "ERROR from underlying socket", e);
			}
			this.receiveOffSet = 0;
			this.receiveLength = dg.getLength();
		}
		int curDataSize = this.receiveLength - this.receiveOffSet;
		if (curDataSize <= len) {
			System.arraycopy(this.receiveBuf, this.receiveOffSet, bytes, offset, curDataSize);
			this.receiveOffSet = -1;
			return curDataSize;
		} else {
			System.arraycopy(this.receiveBuf, this.receiveOffSet, bytes, offset, len);
			this.receiveOffSet += len;
			return len;
		}
	}

	@Override
	public void write(byte[] bytes, int offset, int len) throws TTransportException {
		if (!this.isOpen()) {
			throw new TTransportException(TTransportException.NOT_OPEN);
		}
		if (this.writeBuffer == null) {
			this.writeBuffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
		}
		if (this.writeBuffer.position() + len > MAX_PACKET_SIZE) {
			throw new TTransportException(
					TTransportException.UNKNOWN, "Message size too large: " + len + " > " + MAX_PACKET_SIZE);
		}
		this.writeBuffer.put(bytes, offset, len);
	}

	@Override
	public void flush() throws TTransportException {
		if (this.writeBuffer != null) {
			byte[] bytes = new byte[MAX_PACKET_SIZE];
			int len = this.writeBuffer.position();
			this.writeBuffer.flip();
			this.writeBuffer.get(bytes, 0, len);
			try {
				this.socket.send(new DatagramPacket(bytes, len));
			} catch (IOException e) {
				throw new TTransportException(
						TTransportException.UNKNOWN, "Cannot flush closed transport", e);
			} finally {
				this.writeBuffer = null;
			}
		}
	}

}
