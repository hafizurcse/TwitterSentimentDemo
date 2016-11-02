package org.SentimentSpark.utils

/**
  * Created by dan.dixey on 13/10/2016.
  */
import org.SentimentSpark.core_nlp.CoreNLPSentimentAnalyzer
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector

import scala.util.parsing.json.JSON

object ParsingTweets {

  /**
    * Predicts sentiment of the tweet text with Naive Bayes model passed after removing the stop words.
    *
    * @param text          -- Complete text of a tweet.
    * @param stopWordsList -- Broadcast variable for list of stop words to be removed from the tweets.
    * @param model         -- Naive Bayes Model of the trained data.
    * @return Int Sentiment of the tweet.
    */
  def computeSentiment(text: String, stopWordsList: Broadcast[List[String]], model: NaiveBayesModel): Int = {

    val tweetInWords: Seq[String] = getBarebonesTweetText(text, stopWordsList.value)

    val polarity = model.predict(ParsingTweets.transformFeatures(tweetInWords))

    normalizeMLlibSentiment(polarity)
  }

  /**
    * Normalize sentiment for visualization perspective.
    * We are normalizing sentiment as we need to be consistent with the polarity value with Core NLP and for visualization.
    *
    * @param sentiment polarity of the tweet
    * @return normalized to either -1, 0 or 1 based on tweet being negative, neutral and positive.
    */
  def normalizeMLlibSentiment(sentiment: Double) = {
    sentiment match {
      case x if x == 0 => -1 // negative
      case x if x == 2 => 0 // neutral
      case x if x == 4 => 1 // positive
      case _ => 0 // if cant figure the sentiment, term it as neutral
    }
  }

  /**
    * Strips the extra characters in tweets. And also removes stop words from the tweet text.
    *
    * @param tweetText     -- Complete text of a tweet.
    * @param stopWordsList -- Broadcast variable for list of stop words to be removed from the tweets.
    * @return Seq[String] after removing additional characters and stop words from the tweet.
    */
  def getBarebonesTweetText(tweetText: String, stopWordsList: List[String]): Seq[String] = {
    //Remove URLs, RT, MT and other redundant chars / strings from the tweets.
    tweetText.toLowerCase()
      .replaceAll("\n", "")
      .replaceAll("rt\\s+", "")
      .replaceAll("\\s+@\\w+", "")
      .replaceAll("@\\w+", "")
      .replaceAll("\\s+#\\w+", "")
      .replaceAll("#\\w+", "")
      .replaceAll("(?:https?|http?)://[\\w/%.-]+", "")
      .replaceAll("(?:https?|http?)://[\\w/%.-]+\\s+", "")
      .replaceAll("(?:https?|http?)//[\\w/%.-]+\\s+", "")
      .replaceAll("(?:https?|http?)//[\\w/%.-]+", "")
      .split("\\W+")
      .filter(_.matches("^[a-zA-Z]+$"))
      .filter(!stopWordsList.contains(_))
    //.fold("")((a,b) => a.trim + " " + b.trim).trim
  }

  val hashingTF = new HashingTF()
  /**
    * Transforms features to Vectors.
    *
    * @param tweetText -- Complete text of a tweet.
    * @return Vector
    */
  def transformFeatures(tweetText: Seq[String]): Vector = {
    hashingTF.transform(tweetText)
  }

  /**
    * Extracting Elements from JSON and Classifying it's Sentiment Scores
    *
    * @param inputString -- Complete JSON from the Kafka Topic
    * @param stopWordsList -- Broadcast Spark Reference
    * @param naiveBayesModel -- Machine Learning Model (Naive Bayes)
    */
  def extractData(inputString: String, stopWordsList: Broadcast[List[String]], naiveBayesModel: NaiveBayesModel): List[String] = {
    // Parse the Tweet Data from JSON
    val parseTweet = JSON.parseFull(inputString)

    // Get the Tweet Text
    val text: String = parseTweet match {
      case Some(m: Map[String, String]) => m("text")
      case _ => "Failed"
    }

    // Get the Tweet UserName
    val author_username: String = parseTweet match {
      case Some(m: Map[String, String]) => m("author_username")
      case _ => "Failed"
    }

    // Get the Tweet TimeStamp
    val created_at: String = parseTweet match {
      case Some(m: Map[String, String]) => m("created_at")
      case _ => "Failed"
    }

    // Get a Prediction of the Tweets Sentiment - CoreNLP
    val coreNLP = CoreNLPSentimentAnalyzer.computeWeightedSentiment(text)
    val coreNLPlabel = coreNLP match {
      case -1 => "Negative"
      case 0  => "Neutral"
      case 1  => "Positive"
      case _  => "Not Classified"
    }

    // Get a Prediction of the Tweets Sentiment - Machine Learning Model
    val MLSentiment = ParsingTweets.computeSentiment(text, stopWordsList, naiveBayesModel)
    val MLLIBlabel = MLSentiment match {
      case -1 => "Negative"
      case 0  => "Neutral"
      case 1  => "Positive"
      case _  => "Not Classified"
    }

    // List Return Type
    List(text, author_username, created_at, coreNLPlabel, MLLIBlabel)
  }

}


