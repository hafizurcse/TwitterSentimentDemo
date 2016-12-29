package org.SentimentSpark.utils

import java.util

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}

// Credit for file to:
// https://github.com/xuxiangwen/mastering-spark/blob/426b86b96fea9e45dcf3d85b18d1473d35659228/src/main/scala/org/xxw/spark/streaming/kafka/direct/Producer.scala
case class Producer(props: util.HashMap[String, Object]) {

  @volatile private var instance: KafkaProducer[String, String] = null
  private def getInstance: KafkaProducer[String, String] = {
    if (instance == null) {
      synchronized {
        if (instance == null) {
          instance = new KafkaProducer[String, String](props)
        }
      }
    }
    instance
  }

  def close = {
    synchronized {
      if (instance != null) {
        instance.close
        instance = null
      }
    }
  }

  def send(messages: List[Message]) {
    val producer = new KafkaProducer[String, String](props)
    try {
      messages.foreach(producer.send(_))
    } finally {
      producer.close
    }
  }

  def send(message: Message) {
    send(List(message))
  }

  def sendWithoutClose(messages: List[Message]) {
    val producer = getInstance
    messages.foreach(producer.send(_))
  }

  def sendWithoutClose(message: Message) {
    sendWithoutClose(List(message))
  }

}

object Producer {
  def apply(brokers: String): Producer = {
    val props = new util.HashMap[String, Object]()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
              "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
              "org.apache.kafka.common.serialization.StringSerializer")
    Producer(props)
  }

}
