package org.SentimentSpark.utils

/**
  * Created by dan.dixey on 13/10/2016.
  */
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Exposes all the key-value pairs as properties object using Config object of Typesafe Config project.
  */
object PropertiesLoader {

  private val conf: Config = ConfigFactory.load("application.conf")

  val TrainingFilePath = conf.getString("TRAINING_DATA_PATH")
  val TestingFilePath = conf.getString("TESTING_DATA_PATH")
  val nltkStopWords = conf.getString("NLTK_STOPWORDS_FILE_NAME ")

  val ModelPath = conf.getString("MODEL_PATH")
  val ResultsPath = conf.getString("MODEL_RESULTS ")

  val tweetsClassifiedPath = conf.getString("TWEETS_CLASSIFIED_ABSOLUTE_PATH")

  val consumerKey = conf.getString("CONSUMER_KEY")
  val consumerSecret = conf.getString("CONSUMER_SECRET")
  val accessToken = conf.getString("ACCESS_TOKEN_KEY")
  val accessTokenSecret = conf.getString("ACCESS_TOKEN_SECRET")

  val microBatchTimeInSeconds = conf.getInt("STREAMING_MICRO_BATCH_TIME_IN_SECONDS")
  val totalRunTimeInMinutes = conf.getInt("TOTAL_RUN_TIME_IN_MINUTES")

  val kafka_topic = conf.getString("KAFKA_TOPIC")

}
