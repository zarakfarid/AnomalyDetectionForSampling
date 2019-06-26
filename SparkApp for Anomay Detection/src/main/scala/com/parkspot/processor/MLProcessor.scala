package com.parkspot.processor

import org.apache.log4j.Logger
import org.apache.spark.ml.clustering.{KMeans, KMeansModel}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.iforest.IForest
import org.apache.spark.ml.linalg.{DenseVector, Vector}
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{LongType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.json.{JSONArray, JSONObject}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.mutable.ListBuffer

@Component
class MLProcessor(@Autowired private val sparkSession: SparkSession, @Autowired private val esProcessor: EsProcessor) {

  val logger = Logger getLogger classOf[MLProcessor]

  val schemaKeys = List("JTotalL", "JTotalC", "CreditCardE", "CreditCardL", "PositionE", "Position1L", "AddressE", "AddressL", "MoneyE", "MoneyL", "usernameE", "usernameL", "passwordE", "passwordL", "TraceD", "SpanD1", "SpanD2", "SpanD3", "SpanD4")
  val testSchemaKeys = List("Id", "JTotalL", "JTotalC", "CreditCardE", "CreditCardL", "PositionE", "Position1L", "AddressE", "AddressL", "MoneyE", "MoneyL", "usernameE", "usernameL", "passwordE", "passwordL", "TraceD", "SpanD1", "SpanD2", "SpanD3", "SpanD4")

  var modelTrained: Boolean = false
  var model: PipelineModel = null
  var test: Dataset[Row] = null
  var dataDF: DataFrame = null
  var schema : StructType = null
  var testSchema : StructType = null
  val kmeansClusters = 10

  def predicte(request: String): String = {

    var features  = ListBuffer[ListBuffer[Long]]()
    logger.info("JSON Request"+request)

    val data = new JSONArray(request)
    var i = 0
    var j = 0

    while ({i < data.length}) {
      var durations = ListBuffer[Long]()
      val trace = data.getJSONObject(i)

      val traceID = trace.get("traceID").asInstanceOf[Long]
      durations += trace.get("park-spot").asInstanceOf[Int]
      durations += trace.get("authentication-service").asInstanceOf[Int]
      durations += trace.get("Payment-service").asInstanceOf[Int]
      durations += trace.get("Spot-Finder-service").asInstanceOf[Int]
      durations += trace.get("verification-service").asInstanceOf[Int]

      var json = trace.get("request").asInstanceOf[JSONObject]
      features += esProcessor.generateFeatures(json, durations, traceID)
      i += 1
    }
    features.foreach(println)
    val anamolies = detectAnomalies(features)
    if(anamolies != null){
      val arr =  anamolies.select("Id").rdd.map(r => r(0).asInstanceOf[Long]).collect()
      arr.foreach(println)
      return arr.mkString(",")
    }
    return ""
  }

  def detectAnomalies(features : ListBuffer[ListBuffer[Long]]): DataFrame  = {
    var anomalies : DataFrame = null
    if(modelTrained){
      val rdd = sparkSession.sparkContext.parallelize(features).map(Row.fromSeq)
      val test = sparkSession.createDataFrame(rdd, testSchema)

      val runClustering = new KMeansProcessor(sparkSession, dataDF, testSchema ,"")
      anomalies = runClustering.anomalyDectection(dataDF, model, kmeansClusters, test)
      logger.info("Anomalies:")
      anomalies.show()
    }
    return anomalies
  }

  def trainIsolationForest(request: String): String = {

    if(modelTrained == false) {
      logger.info("Training Model")

      val startTime = System.currentTimeMillis()

      // Dataset from https://archive.ics.uci.edu/ml/datasets/Breast+Cancer+Wisconsin+(Original)
      val dataset = sparkSession.read.option("inferSchema", "true")
        .csv("data/anomaly-detection/breastw.csv")

      dataset.show()
      test = dataset.limit(2)
      dataset.limit(2).show()

      // Index label values: 2 -> 0, 4 -> 1
      val indexer = new StringIndexer()
        .setInputCol("_c10")
        .setOutputCol("label")

      val assembler = new VectorAssembler()
      assembler.setInputCols(Array("_c1", "_c2", "_c3", "_c4", "_c5", "_c6", "_c7", "_c8", "_c9"))
      assembler.setOutputCol("features")

      val iForest = new IForest()
        .setNumTrees(100)
        .setMaxSamples(256)
        .setContamination(0.35)
        .setBootstrap(false)
        .setMaxDepth(100)
        .setSeed(123456L)

      val pipeline = new Pipeline().setStages(Array(indexer, assembler, iForest))
      model = pipeline.fit(dataset)
      modelTrained = true

      val predictions = model.transform(dataset)

      val binaryMetrics = new BinaryClassificationMetrics(
        predictions.select("prediction", "label").rdd.map {
          case Row(label: Double, ground: Double) => (label, ground)
        }
      )

      val endTime = System.currentTimeMillis()
      println(s"Training and predicting time: ${(endTime - startTime) / 1000} seconds.")
      println(s"The model's auc: ${binaryMetrics.areaUnderROC()}")
    }else{
      val t1 = System.nanoTime
      logger.info("Testing Model")
      val xpredictions = model.transform(test)
      //        xpredictions.collect.foreach(println)
      val duration = (System.nanoTime - t1) / 1e9d
      println(s"Predicting time: ${duration} seconds.")
    }

    "Accessing Predictor"
  }

  def trainKMeans()= {

    logger.info("Training Model")

    val startTime = System.currentTimeMillis()

    val traces = esProcessor.fetchTraces()
    traces.foreach(println)

    import sparkSession.sqlContext.implicits._
    val rdd = sparkSession.sparkContext.parallelize(traces).map(Row.fromSeq)
    schema = StructType(
      schemaKeys.map(i => StructField(i, LongType, true))
    )
    testSchema = StructType(
      testSchemaKeys.map(i => StructField(i, LongType, true))
    )
    dataDF = sparkSession.createDataFrame(rdd, schema)
    dataDF.show()

    //    test = dataDF.limit(2);
    //    test.show()

    val runClustering = new KMeansProcessor(sparkSession, dataDF, schema ,"")
    model = runClustering.kmeansSimple(kmeansClusters)
    modelTrained = true
  }




}