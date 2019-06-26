package com.payment.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HttpClientUtil {

	Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);



//	public String sendRequest(Tracer tracer) throws Exception {
//		if(tracer != null && tracer.activeSpan() !=null) {
//			try {
//				String url = "http://localhost:8081/index";
//				logger.info("Sending Request to Backend. URL=" + url);
//				GetMethod method = new GetMethod(url);
//
//				Tags.SPAN_KIND.set(tracer.activeSpan(), Tags.SPAN_KIND_CLIENT);
//				Tags.HTTP_METHOD.set(tracer.activeSpan(), "GET");
//				Tags.HTTP_URL.set(tracer.activeSpan(), "http://localhost:8080/index");
//				tracer.inject(tracer.activeSpan().context(), Format.Builtin.HTTP_HEADERS, new RequestBuilderCarrier(method));
//
//				int statusCode = httpClient.executeMethod(method);
//				String response = method.getResponseBodyAsString();
//				logger.debug("Response=" + response);
//
//				if (statusCode == 403)
//					throw new Exception("Project forbidden! User has no rights to post display records in selected project.");
//				else if (statusCode != 200)
//					throw new Exception("Error occurred while getting SSIMATA Fields Map. Status Code=" + statusCode);
//				logger.debug("Fields Map Response=" + response);
//
//				return response;
//			} catch (Exception e) {
//				e.printStackTrace();
//				throw new Exception("Error occurred while getting SSIMATA Fields Map.", e);
//			}
//		}else {
//				return "Shitssss";
//		}
//	}
}
