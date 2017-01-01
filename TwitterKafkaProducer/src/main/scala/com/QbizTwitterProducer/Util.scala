package com.QbizTwitterProducer

import twitter4j.conf.ConfigurationBuilder

/**
  * Created by dan.dixey on 09/10/2016.
  */
object Util {

  // Twitter Credentials
  private val CONSUMER_KEY = "qjjoV3u8wF0aRxhz8lcxwvXgQ"
  private val CONSUMER_SECRET =
    "dRAhl7QLig13Jgk2qd9S3vwhafDyeiGEiK83NZ73JvYGZErxPK"
  private val ACCESS_TOKEN =
    "15273997-kaf1qbnw3qSQnvC554qsk5PDTJqGUf3xDjhBd7p86"
  private val SECRET_TOKEN = "QXZXJiv9soEJmm1uWogY2L0hMFPj46OKTovy6Lb5PcuPg"

  // Kafka Topic to Pass Messages To
  val KafkaTopic = "ScalaTwitter"

  // Configuration
  val config = new ConfigurationBuilder()
    .setDebugEnabled(true)
    .setOAuthConsumerKey(CONSUMER_KEY)
    .setOAuthConsumerSecret(CONSUMER_SECRET)
    .setOAuthAccessToken(ACCESS_TOKEN)
    .setOAuthAccessTokenSecret(SECRET_TOKEN)
    .setIncludeEntitiesEnabled(true)
    .build

}
