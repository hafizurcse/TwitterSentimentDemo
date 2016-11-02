package com.QbizTwitterProducer

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern._
import akka.util.Timeout
import org.slf4j.LoggerFactory
import twitter4j.{StatusListener, TwitterStream, TwitterStreamFactory}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by dan.dixey on 09/10/2016.
  */
object TwitterStreamApp extends App {

  // Logging information
  val logger = LoggerFactory.getLogger(this.getClass())

  implicit val timeout = Timeout(10 seconds)
  val actorSystem = ActorSystem()

  implicit val dispatcher = actorSystem.dispatcher
  val supervisor = actorSystem.actorOf(Supervisor.props(), "supervisor")

  // Assuming the Kafka is setup locally
  val kafkaProducer = Producer("localhost:9092", dispatcher)
  val topic = Util.KafkaTopic
  val kafkaProducerActor = (supervisor ? (KafkaProducerActor.props(kafkaProducer), "kafkaProducerActor")).mapTo[ActorRef]

  // Defining a Kafka Producer
  kafkaProducerActor.flatMap { producerActor =>

    (supervisor ? (StreamHandler.props(producerActor, topic), "streamHandler")).mapTo[ActorRef]

  }.onComplete {

    case Success(streamHandler) =>

      // Define the Twitter Stream Params:
      val twitterStream: TwitterStream = new TwitterStreamFactory(Util.config).getInstance
      val listener: StatusListener = new TwitterStatusListener(streamHandler)
      twitterStream.addListener(listener)

      // Twitter Filtering
      twitterStream.sample("en")
      twitterStream.filter("data", "analytics", "brexit", "football", "trump", "rooney", "clinton", "russia", "usa",
                           "python", "scala", "qbiz", "qgroup", "lloyds", "gsk", "oil", "pharma")

    // In event an Actor cannot start - log this!
    case Failure(ex) =>
      logger.error("Error in actor initialization ", ex)
      System.exit(0)

  }
}
