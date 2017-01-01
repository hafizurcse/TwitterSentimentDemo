package com.QbizTwitterProducer

import akka.actor.SupervisorStrategy.Resume
import akka.actor.{Actor, OneForOneStrategy, Props, Terminated}
import org.slf4j.LoggerFactory

import scala.concurrent.duration.DurationInt

/**
  * Created by dan.dixey on 09/10/2016.
  */
class Supervisor extends Actor {

  // Logging information
  val logger = LoggerFactory.getLogger(this.getClass())
  val maxNrOfRetries = 10

  // In Actor Systems each actor is the supervisor of its children, and as such each actor defines
  // fault handling supervisor strategy.
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries, withinTimeRange = 1 second)({
      case ex: Exception =>
        logger.error("Actor Instance is dead and Recreating ", ex)
        Resume
    })

  // Handling Data
  def receive: PartialFunction[Any, Unit] = {

    case (props: Props, name: String) =>
      val child = context.actorOf(props, name)
      context.watch(child)
      sender ! child

    case Terminated(child) =>
      logger.error(s"[$child] is dead")

    case invalidMessage =>
      logger.warn("No handler for this message " + invalidMessage)
  }

}

object Supervisor {
  def props() = Props(classOf[Supervisor])

}
