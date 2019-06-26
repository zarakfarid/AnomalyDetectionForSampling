package com.verification.controller;

import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.verification.utils.RequestBuilderCarrier;

import io.opentracing.Scope;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.tag.Tags;


@RestController
public class VerificationController {

	Logger logger = LoggerFactory.getLogger(VerificationController.class);

	@Autowired
	Tracer tracer;
	
	@Autowired
	HttpClient httpClient;

	@RequestMapping(method = RequestMethod.GET, path="/verify")
	public String index(@RequestParam String verify, @RequestHeader MultiValueMap<String, String> rawHeaders) {
	    try (Scope scope = startServerSpan(rawHeaders, "verification-service")) {
	        String helloStr = String.format("Verify, %s!", verify);
	        sendPaymentRequest(verify);
	        scope.span().log(ImmutableMap.of("event", "verification-service", "value", helloStr));
	        return helloStr;
	    }catch(Exception e) {
	    	e.printStackTrace();
	    }
		return "Greetings from Verification Service!";
	}

	public String sendPaymentRequest(String payment) {
		if (tracer != null && tracer.activeSpan() != null) {
			try {
				String url = "http://localhost:8084/pay?payment=" + payment;
				logger.info("Sending Request to Payment. URL=" + url);

				GetMethod method = new GetMethod(url);

				Tags.SPAN_KIND.set(tracer.activeSpan(), Tags.SPAN_KIND_CLIENT);
				Tags.HTTP_METHOD.set(tracer.activeSpan(), "GET");
				Tags.HTTP_URL.set(tracer.activeSpan(), "http://localhost:8084/pay");
				tracer.inject(tracer.activeSpan().context(), Format.Builtin.HTTP_HEADERS,
						new RequestBuilderCarrier(method));

				logger.info("URI:" + method.getURI());
				int statusCode = httpClient.executeMethod(method);
				String response = method.getResponseBodyAsString();
				logger.debug("Response=" + response);

				if (statusCode != 200)
					throw new Exception("Error occurred while Payment. Status Code=" + statusCode);
				logger.debug("Payment Response=" + response);

				return response;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return "No Active Spans";
		}
		return "Payment Failed";
	}
	
	public Scope startServerSpan(MultiValueMap<String, String> rawHeaders, String operationName) {
	    // format the headers for extraction
	    final HashMap<String, String> headers = new HashMap<String, String>();
	    for (String key : rawHeaders.keySet()) {
	        headers.put(key, rawHeaders.get(key).get(0));
	    }
	    Tracer.SpanBuilder spanBuilder;
	    try {
	        SpanContext parentSpan = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapExtractAdapter(headers));
	        if (parentSpan == null) {
	            spanBuilder = tracer.buildSpan(operationName);
	        } else {
	            spanBuilder = tracer.buildSpan(operationName).asChildOf(parentSpan);
	        }
	    } catch (IllegalArgumentException e) {
	        spanBuilder = tracer.buildSpan(operationName);
	    }
	    return spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).startActive(true);
	}
}
