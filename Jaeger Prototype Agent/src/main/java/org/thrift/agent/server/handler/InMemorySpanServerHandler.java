package org.thrift.agent.server.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;
import org.thrift.agent.constants.AgentConstants;
import org.thrift.agent.filter.AgentMLFilter;
import org.thrift.agent.pojo.AgentSpan;

import com.twitter.zipkin.thriftjava.Span;

import io.jaegertracing.agent.thrift.Agent;
import io.jaegertracing.internal.exceptions.SenderException;
import io.jaegertracing.thrift.internal.senders.HttpSender;
import io.jaegertracing.thriftjava.Batch;

public class InMemorySpanServerHandler implements Agent.Iface {

	private ExecutorService executorService = Executors.newFixedThreadPool(1);
	private static Logger logger = LogManager.getLogger(InMemorySpanServerHandler.class);

	private static HttpSender sender = new HttpSender.Builder("http://localhost:14268/api/traces").build();

	private static final boolean flushEverything = true;

	private List<Span> zipkinSpans;
	private io.jaegertracing.thriftjava.Batch batch;

	private static HashMap<Long, AgentSpan> spanMap = new HashMap<>();
	private static HashMap<Long, ArrayList<Long>> traceMap = new HashMap<>();
	private static HashMap<Long, Long> rootTraceIds = new HashMap<>();

	public void emitZipkinBatch(List<Span> spans) throws TException {
		synchronized (this) {
			this.zipkinSpans = spans;
		}
	}

	public void emitBatch(Batch batch) throws TException {
		logger.info("Service Recieved:>>>"+batch.getProcess().getServiceName());
		logger.info("Service Recieved Size:>>>"+batch.getSpans().size());
		if(batch.getSpans().size()>0 && !batch.getProcess().getServiceName().equalsIgnoreCase("jaeger-query")) {
			for(io.jaegertracing.thriftjava.Span span : batch.getSpans()) {
				spanMap.put(span.getSpanId(), new AgentSpan(span, batch.getProcess()));

				if(span.getParentSpanId() != 0) {
					if(traceMap.containsKey(span.getParentSpanId())) {
						traceMap.get(span.getParentSpanId()).add(span.getSpanId());
					}else {
						ArrayList<Long> childern = new ArrayList<Long>();
						childern.add(span.getSpanId());
						traceMap.put(span.getParentSpanId(), childern);
					}	
				}
				if(span.getTraceIdLow() != 0) {
					long countSpanArrived = rootTraceIds.containsKey(span.getTraceIdLow()) ? rootTraceIds.get(span.getTraceIdLow()) : 0;
					rootTraceIds.put(span.getTraceIdLow(), countSpanArrived + 1);
				}

				//				Reached Trace Memory Buffer Size, Now Predicte and Flush
				logger.info("Filtering Traces:"+Collections.frequency(rootTraceIds.values(), AgentConstants.AGENT_SINGLE_TRACE_SPANS));	
				if(Collections.frequency(rootTraceIds.values(), AgentConstants.AGENT_SINGLE_TRACE_SPANS) == AgentConstants.AGENT_FLUSH_TRACE_SIZE && !flushEverything) {
					synchronized(this){
						logger.info("Filtering Traces:"+rootTraceIds.size());	
						executorService.execute(new AgentMLFilter(spanMap, rootTraceIds, traceMap));
					}
				}
				//								System.out.println("*****************************************");
				//								System.out.println("This is the Id High:"+span.getTraceIdHigh());
				//								System.out.println("This is the Id Low:"+span.getTraceIdLow());
				//								System.out.println("This is the Parent Span Id:"+span.getParentSpanId());
				//								System.out.println("This is the Span Id:"+span.getSpanId());
				//								System.out.println("*****************************************");
			}

			//			for (java.util.Map.Entry<Long, Long> entry : rootTraceIds.entrySet()) {
			//				Long key = entry.getKey();
			//				Long value = entry.getValue();
			//				System.out.println("Key:"+key+",Value:"+value);
			//			}

			//			logger.info("Sizeee:"+traceMap.size());
			//			for (java.util.Map.Entry<Long, ArrayList<Long>> entry : traceMap.entrySet()) {
			//				Long key = entry.getKey();
			//				ArrayList<Long> value = entry.getValue();
			//				System.out.println("Key:"+key+",Value:"+value);
			//			}

			//			for(Tag tag : batch.getProcess().getTags()) {
			//				System.out.println("Key:"+tag.key);
			//				System.out.println("Value:"+tag.getVStr());
			//			}
		}
		if(flushEverything) {
			try {
				// System.out.println("Sending Data");
				sender.send(batch.getProcess(), batch.getSpans());
			} catch (SenderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}	
	}

	public List<Span> getZipkinSpans() {
		synchronized (this) {
			if (zipkinSpans != null) {
				return new ArrayList<Span>(zipkinSpans);
			}
			return new ArrayList<Span>();
		}
	}

	public io.jaegertracing.thriftjava.Batch getBatch() {
		synchronized (this) {
			if (batch != null) {
				return batch;
			}
			return new Batch();
		}
	}
}
