package com.QbizTwitterProducer

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}
import scala.util.Try

/**
  * Created by dan.dixey on 09/10/2016.
  */
case class Producer(servers: String, dispatcher: ExecutionContextExecutor) {

  private val props: Properties = new Properties

  props.put("bootstrap.servers", servers)
  props.put("acks", "all")
  props.put("retries", "5")
  props.put("key.serializer", classOf[StringSerializer].getName)
  props.put("value.serializer", classOf[StringSerializer].getName)

  private val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](props)


  def send(topic: String, record: String): Future[RecordMetadata] = {
    val message: ProducerRecord[String, String] = new ProducerRecord[String, String](topic, record, record)

    val recordMetadataResponse = producer.send(message)
    val promise = Promise[RecordMetadata]()

    Future {
      promise.complete(Try(recordMetadataResponse.get()))
    }(dispatcher)
      promise.future
  }

  def close(): Unit = producer.close()

}


