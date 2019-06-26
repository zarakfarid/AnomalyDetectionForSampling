package com.parkspot.config

import java.net.InetAddress

import org.apache.http.HttpHost
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.TransportAddress
import org.springframework.context.annotation.{Bean, Configuration}
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient

@Configuration
class ElasticSearchConfig {

  @Bean
  def client = {
    new RestHighLevelClient(
      RestClient.builder(new HttpHost("localhost", 9200)))
    }
}
