package com.parkspot.config


import org.apache.spark.sql.SparkSession
import org.springframework.context.annotation.Configuration
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean

@Configuration
class SparkConfig {

  @Bean
  def sparkSession: SparkSession = {
    val spark = SparkSession
      .builder()
      .master(master = "local") // test in local mode
      .appName(name = "iforest example")
      .getOrCreate()
    return spark
  }

}
