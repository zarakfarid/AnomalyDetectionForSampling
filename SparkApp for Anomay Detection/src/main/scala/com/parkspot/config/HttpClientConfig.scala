package com.parkspot.config

import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager
import org.apache.commons.httpclient.params.HttpConnectionManagerParams
import org.apache.commons.httpclient.HttpClient
import org.springframework.context.annotation.{Bean, Configuration}
@Configuration
class HttpClientConfig {

  @Bean
  def httpClient = {
    val httpClient = new HttpClient()
    httpClient.setHttpConnectionManager(getmttpConnection)
    httpClient.getHttpConnectionManager.getParams.setSoTimeout(0)
    httpClient
  }

  def getmttpConnection = {
    val hcmp = new HttpConnectionManagerParams
    hcmp.setDefaultMaxConnectionsPerHost(5)
    hcmp.setMaxTotalConnections(10)
    val mthttpConnection = new MultiThreadedHttpConnectionManager
    mthttpConnection.setParams(hcmp)
    mthttpConnection
  }
}
