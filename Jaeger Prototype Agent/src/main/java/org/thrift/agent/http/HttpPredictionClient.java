package org.thrift.agent.http;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.thrift.agent.filter.AgentMLFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class HttpPredictionClient {

	private static Logger logger = LogManager.getLogger(HttpPredictionClient.class);

	private static HttpClient httpClient = HttpClient();

	public static HttpClient HttpClient() {
		HttpClient httpClient = new HttpClient();
		httpClient.setHttpConnectionManager(getmttpConnection());
		httpClient.getHttpConnectionManager().getParams().setSoTimeout(0);
		return httpClient;
	}

	private static MultiThreadedHttpConnectionManager getmttpConnection() {
		HttpConnectionManagerParams hcmp = new HttpConnectionManagerParams();
		hcmp.setDefaultMaxConnectionsPerHost(5);
		hcmp.setMaxTotalConnections(10);
		MultiThreadedHttpConnectionManager mthttpConnection = new MultiThreadedHttpConnectionManager();
		mthttpConnection.setParams(hcmp);
		return mthttpConnection;
	}

	public static String sendPredictionRequest(JSONArray features) {
		try {
			String url = "http://localhost:8085/predicte";
			logger.info("Sending Request to Authentication Service. URL=" + url);
			
			StringRequestEntity requestEntity = new StringRequestEntity(
					features.toString(),
				    "application/json",
				    "UTF-8");
			
			PostMethod method = new PostMethod(url);
			method.addRequestHeader("Content-Type", "application/json");
			method.setRequestEntity(requestEntity);

			logger.info("URI:" + method.getURI());
			int statusCode = httpClient.executeMethod(method);
			String response = method.getResponseBodyAsString();

			if (statusCode != 200)
				throw new Exception("Error occurred while Authenticating. Status Code=" + statusCode);
			logger.info("Prediction Response=" + response);

			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Prediction Request Failed";
	}
}
