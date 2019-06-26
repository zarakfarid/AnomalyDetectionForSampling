package com.spotFinder.application;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.jaegertracing.internal.JaegerTracer;

@SpringBootApplication
@ComponentScan("com.*")
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    
    @Bean
    public JaegerTracer  initTracer() {
        SamplerConfiguration samplerConfig = SamplerConfiguration.fromEnv().withType("const").withParam(1);
        ReporterConfiguration reporterConfig = ReporterConfiguration.fromEnv().withLogSpans(true);
        Configuration config = new Configuration("Spot-Finder-Serivce").withSampler(samplerConfig).withReporter(reporterConfig);
        return config.getTracer();
    }
	/**
	 * Instantiates a new http client util.
	 */
	@Bean
	public HttpClient HttpClient() {
		HttpClient httpClient = new HttpClient();
		httpClient.setHttpConnectionManager(getmttpConnection());
		httpClient.getHttpConnectionManager().getParams().setSoTimeout(0);
		return httpClient;
	}
	
    private MultiThreadedHttpConnectionManager getmttpConnection() {
		HttpConnectionManagerParams hcmp = new HttpConnectionManagerParams();
		hcmp.setDefaultMaxConnectionsPerHost(5);
		hcmp.setMaxTotalConnections(10);
		MultiThreadedHttpConnectionManager mthttpConnection = new MultiThreadedHttpConnectionManager();
		mthttpConnection.setParams(hcmp);
		return mthttpConnection;
	}

}
