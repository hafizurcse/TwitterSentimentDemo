package org.SentimentSpark

import java.util.Properties

import com.typesafe.scalalogging.LazyLogging
import kafka.producer.{Producer, ProducerConfig}
import org.SentimentSpark.utils.{NLTK_loader, ParsingTweets, PropertiesLoader}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Durations, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by dan.dixey on 14/10/2016.
  */
object KafkaConsumerApp extends App with LazyLogging {

  override def main(args: Array[String]): Unit = {

    // Setup the Spark Environment
    val conf = new SparkConf()
      .setMaster("local[*]")
      .setAppName("KafkaListener")
      .set("spark.serializer", classOf[KryoSerializer].getCanonicalName)
    val sc = SparkContext.getOrCreate(conf)
    val ssc = new StreamingContext(sc, Durations.seconds(PropertiesLoader.microBatchTimeInSeconds))
    //val kafkaSink = sc.broadcast(KafkaSink(conf))
    logger.info("Spark Configuration Loaded")

    // Load the training Naive Bayes Model
    val naiveBayesModel = NaiveBayesModel.load(ssc.sparkContext, PropertiesLoader.ModelPath)
    val stopWordsList = ssc.sparkContext.broadcast(NLTK_loader.loadStopWords(PropertiesLoader.nltkStopWords))
    logger.info("Spark MLlib Model Loaded")

    // Connect to this Kakfa Topic
    val kafkaListening = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "example",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    // Pushing to Kafka
    val props:Properties = new Properties()
      props.put("metadata.broker.list", "localhost:9092")
      props.put("serializer.class", "kafka.serializer.StringEncoder")
    val config = new ProducerConfig(props)
    val producer = new Producer[String, String](config)

    val topic = PropertiesLoader.kafka_topic
    val topicMap = Map[String, Int](topic -> 4)

    // Create the stream
    val kafkaStream = KafkaUtils.createStream(ssc, "localhost:2181", "Dan", topicMap)
    val numInputMessages = ssc.sparkContext.doubleAccumulator("Kafka messages consumed")
    val numMessagesPositive = ssc.sparkContext.doubleAccumulator("Positive Tweets")
    val numMessagesNegative = ssc.sparkContext.doubleAccumulator("Negative Tweets")
    val numMessagesNeutral = ssc.sparkContext.doubleAccumulator("Neutral Tweets")
    logger.info("Kafka Configuration Set")

    // Whenever this Kafka stream produces data the resulting RDD will be printed
    kafkaStream.foreachRDD(r => {
      if (r.count() > 0) {
        logger.debug("Got RDD, size = " + r.count() )

        numInputMessages.add(r.count() )
        r.foreach(s => {


          // Parse the Tweets
          val transform: String = ParsingTweets.extractData(s._1, stopWordsList, naiveBayesModel,
                               numMessagesPositive, numMessagesNegative, numMessagesNeutral)

          // Print the Output
          println(transform)

          // Push Message as JSON to Kafka Queue
          //producer.send(new KeyedMessage[String, String]("KafkaWebApp", transform.toString))

        })

      }
    })

    // Start the Spark Streaming Context
    ssc.start()
    logger.info("Spark Streaming Context Running")

    // Give the stream time to initialize
    Thread.sleep(2000)
    logger.info("Listening to Kafka")

    // Auto-kill after processing rawTweets for n minutes.
    ssc.awaitTerminationOrTimeout(PropertiesLoader.totalRunTimeInMinutes * 60 * 1000)
    logger.info("Spark Streaming Halted")

    // Get some Metrics about the processing!
    logger.info("Tweets Processed : " + numInputMessages.value )
    logger.info("Positive Tweets Processed : " + numMessagesPositive.value )
    logger.info("Negative Tweets Processed : " + numMessagesNegative.value )
    logger.info("Neutral Tweets Processed : " + numMessagesNeutral.value )

    logger.info("Stopped Listening to Kafka")
    ssc.stop(stopSparkContext = true, stopGracefully = true)
    logger.info("App Shutting Down")
  }
}
