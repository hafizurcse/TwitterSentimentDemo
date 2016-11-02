package org.SentimentSpark.utils

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext

/**
  * Created by dan.dixey on 13/10/2016.
  * Reduce the console logging during execution of Spark jobs.
  */
object LogUtils{

  val log = Logger.getLogger(getClass.getName)

  /**
    * Setting the logging level for the project
    *
    * @param sparkContext -- Spark Context
    */
  def setLogLevels(sparkContext: SparkContext) {

    sparkContext.setLogLevel(Level.WARN.toString)
    val log4jInitialized = Logger.getRootLogger.getAllAppenders.hasMoreElements
    if (!log4jInitialized) {
      log.info(
        """Setting log level to [WARN] for streaming executions.
          |To override add a custom log4j.properties to the classpath.""".stripMargin)
      Logger.getRootLogger.setLevel(Level.WARN)

    }
  }
}
