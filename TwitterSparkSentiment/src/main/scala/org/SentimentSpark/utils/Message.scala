package org.SentimentSpark.utils

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord

object Message {
  def apply(topic: String, value: String): Message = {
    Message(topic, "", value, -1, -1)
  }

  def apply(topic: String, key: String, value: String): Message = {
    Message(topic, key, value, -1, -1)
  }

  def apply(topic: String,
            key: String,
            value: String,
            partition: Int): Message = {
    Message(topic, key, value, partition, -1)
  }

  implicit def messageToProducerRecord(message: Message) = {
    val key = if (message.key == "") null else message.key
    if (message.partition >= 0) {
      new ProducerRecord(message.topic, message.partition, key, message.value)
    } else {
      new ProducerRecord(message.topic, message.key, message.value)
    }
  }

  implicit def consumerRecordToMessage(
      record: ConsumerRecord[String, String]) = {
    val key = if (record.key == null) "" else record.key
    Message(record.topic, key, record.value, record.partition, record.offset)
  }
}

case class Message(topic: String,
                   key: String,
                   value: String,
                   partition: Int,
                   offset: Long)
    extends java.io.Serializable {}
