package com.park.spot.controller;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.park.spot.common.data.tracingRequest;
import com.park.spot.utils.RequestBuilderCarrier;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;

@RestController
public class ParkSpotController {

	Logger logger = LoggerFactory.getLogger(ParkSpotController.class);

	private static BloomFilter<String> bloomFilter = getBloomFilter();

	@Autowired
	Tracer tracer;

	@Autowired
	private HttpClient httpClient;

	@RequestMapping(method = RequestMethod.POST, path = "/index")
	public String index(@RequestBody String request) {
		try (Scope scope = tracer.buildSpan("park-spot").startActive(true)) {
			scope.span().setTag("request", request);
			//			if (detectAnomaly(request)) {
			//				Tags.SAMPLING_PRIORITY.set(tracer.activeSpan(), 1);
			//			}
			authenticate(request);
			verification(request);
			findSpot(request);
			scope.span().finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Greetings from Parking Spot Service!";
	}

	private String findSpot(String request) {
		String response = null;
		try (Scope scope = tracer.buildSpan("spotFinder").startActive(true)) {
			response = sendSpotFinderRequest();
			logger.info(response);
			scope.span().finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;		
	}

	private boolean verification(String request) {
		boolean response = false;
		try (Scope scope = tracer.buildSpan("verification").startActive(true)) {
			response = sendVerificationRequest();
			scope.span().finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	private String authenticate(String request) {
		String response = null;
		try (Scope scope = tracer.buildSpan("authentication").startActive(true)) {
			response = sendAuthenticationRequest();
			logger.info(response);
			scope.span().finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	private String sendSpotFinderRequest() {

		if (tracer != null && tracer.activeSpan() != null) {
			try {
				String url = "http://localhost:8083/findSpot?coordinates=" + "zarakRootService";
				logger.info("Sending Request to Spot Finder. URL=" + url);

				GetMethod method = new GetMethod(url);

				Tags.SPAN_KIND.set(tracer.activeSpan(), Tags.SPAN_KIND_CLIENT);
				Tags.HTTP_METHOD.set(tracer.activeSpan(), "GET");
				Tags.HTTP_URL.set(tracer.activeSpan(), "http://localhost:8083/findSpot");
				tracer.inject(tracer.activeSpan().context(), Format.Builtin.HTTP_HEADERS,
						new RequestBuilderCarrier(method));

				logger.info("URI:" + method.getURI());
				int statusCode = httpClient.executeMethod(method);
				String response = method.getResponseBodyAsString();
				logger.debug("Response=" + response);

				if (statusCode != 200)
					throw new Exception("Error occurred while Finding Spot. Status Code=" + statusCode);
				logger.debug("Spot Finder Response=" + response);

				return response;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return "No Active Spans";
		}
		return "Finding Spot Failed";
	}

	private boolean sendVerificationRequest() {

		if (tracer != null && tracer.activeSpan() != null) {
			try {
				String url = "http://localhost:8082/verify?verify=" + "zarakRootService";
				logger.info("Sending Request to Verification Sevice. URL=" + url);

				GetMethod method = new GetMethod(url);

				Tags.SPAN_KIND.set(tracer.activeSpan(), Tags.SPAN_KIND_CLIENT);
				Tags.HTTP_METHOD.set(tracer.activeSpan(), "GET");
				Tags.HTTP_URL.set(tracer.activeSpan(), "http://localhost:8082/verify");
				tracer.inject(tracer.activeSpan().context(), Format.Builtin.HTTP_HEADERS,
						new RequestBuilderCarrier(method));

				logger.info("URI:" + method.getURI());
				int statusCode = httpClient.executeMethod(method);
				String response = method.getResponseBodyAsString();
				logger.debug("Response=" + response);

				if (statusCode != 200)
					throw new Exception("Error occurred while Authenticating. Status Code=" + statusCode);
				logger.debug("Verification Response=" + response);

				return response != null && response.equalsIgnoreCase("verified") ? true : false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return false;
		}
		return false;
	}

	public String sendAuthenticationRequest() {
		if (tracer != null && tracer.activeSpan() != null) {
			try {
				String url = "http://localhost:8081/authenticate?user=" + "zarakRootService";
				logger.info("Sending Request to Authentication Service. URL=" + url);

				GetMethod method = new GetMethod(url);

				Tags.SPAN_KIND.set(tracer.activeSpan(), Tags.SPAN_KIND_CLIENT);
				Tags.HTTP_METHOD.set(tracer.activeSpan(), "GET");
				Tags.HTTP_URL.set(tracer.activeSpan(), "http://localhost:8081/authenticate");
				tracer.inject(tracer.activeSpan().context(), Format.Builtin.HTTP_HEADERS,
						new RequestBuilderCarrier(method));

				logger.info("URI:" + method.getURI());
				int statusCode = httpClient.executeMethod(method);
				String response = method.getResponseBodyAsString();
				logger.debug("Response=" + response);

				if (statusCode != 200)
					throw new Exception("Error occurred while Authenticating. Status Code=" + statusCode);
				logger.debug("Authentication Response=" + response);

				return response;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return "No Active Spans";
		}
		return "Authentication Failed";
	}

	private static BloomFilter<String> getBloomFilter() {
		BloomFilter<String> filter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 500, 0.01);
		Field[] fields = tracingRequest.class.getDeclaredFields();
		for (Field f : fields) {
			filter.put(f.getName());
		}
		return filter;
	}

	private boolean detectAnomaly(String request) {
		boolean anomalyDetected = false;

		logger.info("Request:" + request);
		JsonObject obj = new JsonParser().parse(request).getAsJsonObject();
		Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();// will return members of your object
		long bloomSize = bloomFilter.approximateElementCount();
		long size = 0;
		for (Map.Entry<String, JsonElement> entry : entries) {
			String key = entry.getKey();
			logger.info("Json Key:" + key);
			if (bloomFilter.mightContain(key)) {
				size++;
			}
		}

		if (bloomSize != size) {
			anomalyDetected = true;
		}

		logger.info("Is it Anomaly:" + anomalyDetected);
		return anomalyDetected;
	}

}
