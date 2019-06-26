package com.parkspot.controller

import com.parkspot.processor.{EsProcessor, MLProcessor}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{GetMapping, PostMapping, RequestBody, RestController}


@RestController
class PredictorController(@Autowired private val predictor: MLProcessor) {

  val logger = Logger getLogger classOf[PredictorController]

  @PostMapping(path = Array("/predicte"))
  def demo(@RequestBody request: String)={
    logger.info("Request:"+request)
    predictor.predicte(request)
  }

  @GetMapping(path = Array("/train"))
  def search(@RequestBody request: String)={
    logger.info("Request:"+request)
    predictor.trainKMeans()
  }



}
