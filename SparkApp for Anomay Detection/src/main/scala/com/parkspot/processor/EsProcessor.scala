package com.parkspot.processor

import java.util

import org.elasticsearch.client.{RequestOptions, RestHighLevelClient}
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.{PostMethod, StringRequestEntity}
import org.apache.log4j.Logger
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

@Component
class EsProcessor(@Autowired private val client: RestHighLevelClient, @Autowired private val httpClient: HttpClient) {

  val logger = Logger getLogger classOf[EsProcessor]

  val jsonSchemaKeys = List("CreditCard", "Position", "Address", "Money", "username", "password")

  def generateFeatures(data: JSONObject, duration: ListBuffer[Long], traceID: Long) = {
    var features = ListBuffer[Long]()
    if(traceID != 0){
      features += traceID
    }
    //Length of the JSON
    features += data.toString().length
    //Items in the JSON
    features += data.length

    for(jsonSchemaKey <- jsonSchemaKeys){

      if(!data.has(jsonSchemaKey)){
        features += 0
        features += 0
      }else{
        features += 1
        features += (data.get(jsonSchemaKey).toString.length)
      }
    }
    features.addAll(duration)
    features
  }

  def getRecordsFromTraceId(traceId: String) = {
    var durations = ListBuffer[Long]()

    val qb = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("traceID", traceId)).must(QueryBuilders.regexpQuery("operationName", ".*service"))
    val searchSourceBuilder = new SearchSourceBuilder()
    searchSourceBuilder.query(qb)
    searchSourceBuilder.sort("operationName")

    var searchRequest = new SearchRequest("jaeger-span-*")
    searchRequest.types("span")
    searchRequest.source(searchSourceBuilder)

    val searchResponse = client.search(searchRequest,  RequestOptions.DEFAULT)
    val totalHits = searchResponse.getHits.totalHits
    for (hit <- searchResponse.getHits.getHits) {
      val source = hit.getSourceAsMap
      durations += source.get("duration").asInstanceOf[Int]
    }
    durations
  }

  def getRecords(startFrom: Int, size: Int) = {

    var features  = ListBuffer[ListBuffer[Long]]()

    val searchSourceBuilder = new SearchSourceBuilder()
    searchSourceBuilder.query(QueryBuilders.termQuery("operationName", "park-spot"))
    searchSourceBuilder.from(startFrom)
    searchSourceBuilder.size(size)

    var searchRequest = new SearchRequest("jaeger-span-*")
    searchRequest.types("span")
    searchRequest.source(searchSourceBuilder)

    val searchResponse = client.search(searchRequest,  RequestOptions.DEFAULT)
    for (hit <- searchResponse.getHits.getHits) {

      val source = hit.getSourceAsMap

      var durations = ListBuffer[Long]()
      if(source.containsKey("duration")){
        durations += source.get("duration").asInstanceOf[Int]
      }

      if(source.containsKey("traceID")){
        val traceId =  source.get("traceID").asInstanceOf[String]
        durations.addAll(getRecordsFromTraceId(traceId))
      }

      if(source.containsKey("tags")){
        val tags = source.get("tags").asInstanceOf[java.util.List[util.HashMap[java.lang.String, Object]]]
        for(tag <- tags){
          if(tag.get("key").equals("request")){
            val value = tag.get("value").toString

            val data = new JSONObject(value)
            features += generateFeatures(data, durations, 0)
          }
        }
      }
    }
    features
  }

  def getTotalTraceCounts(): Long = {
    try {
      val url = "http://localhost:9200/jaeger-span-*/_count"
      logger.info("Sending Request to ElasticSearch. URL=" + url)
      val requestEntity = new StringRequestEntity("{\n  \"query\": {\n            \"match\" : {\n            \"operationName\" : \"park-spot\"\n        }\n  }\n}", "application/json", "UTF-8")
      val method = new PostMethod(url)
      method.addRequestHeader("Content-Type", "application/json")
      method.setRequestEntity(requestEntity)
      logger.info("URI:" + method.getURI)
      val statusCode = httpClient.executeMethod(method)
      val response = method.getResponseBodyAsString
      if (statusCode != 200) throw new Exception("Error occurred while ElasticSearch Search. Status Code=" + statusCode)
      logger.debug("ElasticSearch Response=" + response)
      return new JSONObject(response).getLong("count")
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
    0
  }

  def fetchTraces() = {

    //For Latest Elastic Version
    //    val countRequest = new CountRequest("jaeger-span-*")
    //    countRequest.types("span")
    //
    //    val searchSourceBuilder = new SearchSourceBuilder()
    //    searchSourceBuilder.query(QueryBuilders.termQuery("operationName", "park-spot"))
    //    countRequest.source(searchSourceBuilder)
    //
    //    val countResponse = client.count(countRequest, RequestOptions.DEFAULT)
    //    val recordsCount = countResponse.getCount
    //    logger.info("Total Count is:"+countResponse.getCount)

    val traceCount = getTotalTraceCounts
    logger.info("Count:" + traceCount)
    var allFeatures  = ListBuffer[ListBuffer[Long]]()
    var startFrom = 0
    if (traceCount > 0) {
      if (traceCount > 50) {
        while (startFrom <= traceCount) {
          allFeatures.addAll(getRecords(startFrom, 50))
          startFrom += 50
        }
      } else {
        allFeatures.addAll(getRecords(startFrom, 50))
      }
    }
    allFeatures
  }
}
