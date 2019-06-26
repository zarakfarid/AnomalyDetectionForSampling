package org.thrift.agent.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.thrift.agent.constants.AgentConstants;
import org.thrift.agent.http.HttpPredictionClient;
import org.thrift.agent.pojo.AgentSpan;

import io.jaegertracing.internal.exceptions.SenderException;
import io.jaegertracing.thrift.internal.senders.HttpSender;
import io.jaegertracing.thriftjava.Batch;
import io.jaegertracing.thriftjava.Process;
import io.jaegertracing.thriftjava.Span;
import io.jaegertracing.thriftjava.Tag;

public class AgentMLFilter extends Thread {

	private static Logger logger = LogManager.getLogger(AgentMLFilter.class);

	private static HttpSender sender = new HttpSender.Builder("http://localhost:14268/api/traces").build();

	private HashMap<Long, AgentSpan> spanMap = null;
	private HashMap<Long, ArrayList<Long>> traceMap = null;
	private HashMap<Long, Long> rootTraceIds = null;
	private JSONArray features = null;

	public AgentMLFilter(HashMap<Long, AgentSpan> spanMap, HashMap<Long, Long> rootTraceIds,
			HashMap<Long, ArrayList<Long>> traceMap) {
		this.spanMap = spanMap;
		this.rootTraceIds = rootTraceIds;
		this.traceMap = traceMap;
		this.features = new JSONArray();
	}

	public void run() {
		ArrayList<Long> traceToRemoveFromMemory = new ArrayList<>();
		ArrayList<Long> spansToRemoveFromMemory = new ArrayList<>();
		// Reached Trace Memory Buffer Size, Now Predicte, Flush to Collector and Remove from Memory

		long incomingTraceId = 0;
		////////////////////////////////////////////////////////////////////////////////////////////////////  
		try {
			for (Iterator<Map.Entry<Long, Long>> it = rootTraceIds.entrySet().iterator(); it.hasNext();) {
				Map.Entry<Long, Long> entry = it.next();
				long key = incomingTraceId = entry.getKey();
				long spanCount = entry.getValue();

				logger.info("Sending Request for Trace Id:"+key+", with Spans"+spanCount);
				if (spanCount == AgentConstants.AGENT_SINGLE_TRACE_SPANS) {
					traceToRemoveFromMemory.add(key);

					ArrayList<AgentSpan> spans = fetchAllSpansForTrace(key);
					JSONObject spanFeatures = new JSONObject();
					spanFeatures.put("traceID", key);
					for (AgentSpan agentSpan : spans) {
						
						if (agentSpan.getSpan() != null) {
							spansToRemoveFromMemory.add(agentSpan.getSpan().getSpanId());

							if (agentSpan.getSpan().getParentSpanId() == 0) {
								for (Tag tag : agentSpan.getSpan().getTags()) {
									if (tag.key.equals("request")) {
										JSONObject jsonObj = new JSONObject(tag.getVStr());
										spanFeatures.put("request", jsonObj);
										spanFeatures.put(agentSpan.getSpan().operationName, agentSpan.getSpan().getDuration());
									}
								}
							} else if (agentSpan.getSpan().operationName.contains("service")) {
								spanFeatures.put(agentSpan.getSpan().operationName, agentSpan.getSpan().getDuration());
							}
						}
					}
					features.put(spanFeatures);
				} else {
					logger.info("All Spans for TraceId:" + key + " have not arrived");
				}
			}
		}catch(Exception e) {
			logger.error("Exception in Extracting Data from features");
			e.printStackTrace();
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////  

		if(features.length() > 0) {
			rootTraceIds.keySet().removeAll(traceToRemoveFromMemory);
			
			String anomalousTraces = null;
			try {
				////////////////////////////////////////////////////////////////////////////////////////////////////  
				anomalousTraces = HttpPredictionClient.sendPredictionRequest(features);
//				anomalousTraces = String.valueOf(incomingTraceId);
				////////////////////////////////////////////////////////////////////////////////////////////////////  
			}catch(Exception e) {
				logger.error("Exception in Sending Prediction Request");
				e.printStackTrace();
			}

			try {
				if(anomalousTraces !=null && anomalousTraces.length()>0) {
					String[] anamolousIds = anomalousTraces.split(",");
					ArrayList<Span> spans = new ArrayList<>();

					Process oldProcess = null;
					for(String traceId : anamolousIds) {
						ArrayList<AgentSpan> traceSpans = fetchAllSpansForTrace(Long.valueOf(traceId));

						for(AgentSpan traceSpan : traceSpans) {
							Process newProcess = traceSpan.getProcess();
							////////////////////////////
							if(spans.size()>0) {
								if(!newProcess.equals(oldProcess)) {
									sendBatchToCollector(oldProcess , spans);
									spans = new ArrayList<>();
									spans.add(traceSpan.getSpan());
								}else {
									spans.add(traceSpan.getSpan());
								}
							}else {
								spans.add(traceSpan.getSpan());
							}
							////////////////////////////
							oldProcess = traceSpan.getProcess();
						}
					}

					if(spans.size()>0) {
						sendBatchToCollector(oldProcess , spans);
					}
				}		
			}catch(Exception e) {
				logger.error("Exception in Sending Batch Request to Collector");
				e.printStackTrace();
			}
			
			spanMap.keySet().removeAll(spansToRemoveFromMemory);
			traceMap.keySet().removeAll(spansToRemoveFromMemory);
		}
	}

	private void sendBatchToCollector(Process process, ArrayList<Span> spans) {
		try {
			sender.send(process, spans);
		} catch (SenderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	private ArrayList<AgentSpan> fetchAllSpansForTrace(long key) {
		ArrayList<AgentSpan> spans = null;
		if (traceMap.containsKey(key) && spanMap.containsKey(key)) {
			spans = new ArrayList<>();
			spans.add(spanMap.get(key));
			getSpans(traceMap.get(key), spans);
		} else {
			logger.info("Trace doesnot Exists for Id:" + key);
		}
		return spans;
	}

	private ArrayList<AgentSpan> getSpans(ArrayList<Long> spanIds, ArrayList<AgentSpan> spans) {
		for (long spanId : spanIds) {
			if (traceMap.containsKey(spanId) && spanMap.containsKey(spanId)) {
				spans.add(spanMap.get(spanId));
				getSpans(traceMap.get(spanId), spans);
			} else if (spanMap.containsKey(spanId)) {
				spans.add(spanMap.get(spanId));
			}
		}
		return spans;
	}
}
