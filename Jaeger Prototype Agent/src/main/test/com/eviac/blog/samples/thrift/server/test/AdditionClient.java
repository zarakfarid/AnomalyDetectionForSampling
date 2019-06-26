package com.eviac.blog.samples.thrift.server.test;
//package org.thrift.agent;
//
//import org.apache.thrift.TException;
//import org.apache.thrift.protocol.TBinaryProtocol;
//import org.apache.thrift.protocol.TProtocol;
//import org.apache.thrift.transport.TSocket;
//import org.apache.thrift.transport.TTransport;
//import org.apache.thrift.transport.TTransportException;
//
//import com.eviac.blog.samples.thrift.server.AdditionService;
//
//public class AdditionClient {
//
// public static void main(String[] args) {
//
//  try {
//   TTransport transport;
//
//   transport = new TSocket("localhost", 6831);
//   transport.open();
//
//   TProtocol protocol = new TBinaryProtocol(transport);
//   AdditionService.Client client = new AdditionService.Client(protocol);
//
//   client.emitBatch(null);
//
//   transport.close();
//  } catch (TTransportException e) {
//   e.printStackTrace();
//  } catch (TException x) {
//   x.printStackTrace();
//  }
// }
//
//}
