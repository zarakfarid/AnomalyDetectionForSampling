package com.authentication.controller;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import io.opentracing.Scope;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.tag.Tags;


@RestController
public class AuthenticationController {

	Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

	@Autowired
	Tracer tracer;
	
	@RequestMapping(method = RequestMethod.POST, path="/traces")
	public String traces(@RequestBody String hello) {
		logger.info(hello);
		return "Greetings from Authentication Service!";
	}

	@RequestMapping(method = RequestMethod.GET, path="/authenticate")
	public String index(@RequestParam String user, @RequestHeader MultiValueMap<String, String> rawHeaders) {	
	    try (Scope scope = startServerSpan(rawHeaders, "authentication-service")) {
	        String User = String.format("Authenticate, %s!", user);
	        scope.span().log(ImmutableMap.of("event", "authentication-service", "value", User));
	        return User;
	    }catch(Exception e) {
	    	e.printStackTrace();
	    }
		return "Greetings from Authentication Service!";
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
