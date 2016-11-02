package org.SentimentSpark.sparkML

/**
  * Created by dan.dixey on 13/10/2016.
  */
import org.SentimentSpark.core_nlp.CoreNLPSentimentAnalyzer
import org.SentimentSpark.utils._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql._
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Creates a Model of the training dataset using Spark MLlib's Naive Bayes classifier.
  */
object TrainModelsNB {

  def main(args: Array[String]) {
    println("Starting Spark")
    val sc = createSparkContext()

    LogUtils.setLogLevels(sc)

    val stopWordsList = sc.broadcast(NLTK_loader.loadStopWords(PropertiesLoader.nltkStopWords))
    createAndSaveNBModel(sc, stopWordsList)
    validateAccuracyOfNBModel(sc, stopWordsList)

    println("Stopping Spark")
    sc.stop()

    println("Exiting App")
    System.exit(1)
  }

  /**
    * Remove new line characters.
    *
    * @param tweetText -- Complete text of a tweet.
    * @return String with new lines removed.
    */
  def replaceNewLines(tweetText: String): String = {
    tweetText.replaceAll("\n", "")
  }

  /**
    * Create SparkContext
    *
    * @return SparkContext
    */
  def createSparkContext(): SparkContext = {
    val conf = new SparkConf()
      .setMaster("local[*]")
      .setAppName("NaiveBayes")
      .set("spark.serializer", classOf[KryoSerializer].getCanonicalName)

    val sc = SparkContext.getOrCreate(conf)
    sc
  }

  /**
    * Creates a Naive Bayes Model of Tweet and its Sentiment from the Sentiment140 file.
    *
    * @param sc            -- Spark Context.
    * @param stopWordsList -- Broadcast variable for list of stop words to be removed from the tweets.
    */
  def createAndSaveNBModel(sc: SparkContext, stopWordsList: Broadcast[List[String]]): Unit = {

    val tweetsDF: DataFrame = loadSentiment140File(sc, PropertiesLoader.TrainingFilePath)

    val labeledRDD = tweetsDF.select("polarity", "status").rdd.map {

      case Row(polarity: Int, tweet: String) =>

        val tweetInWords: Seq[String] = ParsingTweets.getBarebonesTweetText(tweet, stopWordsList.value)

        LabeledPoint(polarity, ParsingTweets.transformFeatures(tweetInWords))

    }
    // Store in cache memory
    labeledRDD.cache()

    val naiveBayesModel: NaiveBayesModel = NaiveBayes.train(labeledRDD, lambda = 1.0, modelType = "multinomial")

    // TODO: Delete Model in Dir if it exists Prior to Saving
    naiveBayesModel.save(sc, PropertiesLoader.ModelPath)
  }

  /**
    * Validates and check the accuracy of the model by comparing the polarity of a tweet from the
    * dataset and compares it with the MLlib predicted polarity.
    *
    * @param sc            -- Spark Context.
    * @param stopWordsList -- Broadcast variable for list of stop words to be removed from the tweets.
    */
  def validateAccuracyOfNBModel(sc: SparkContext, stopWordsList: Broadcast[List[String]]): Unit = {
    val naiveBayesModel: NaiveBayesModel = NaiveBayesModel.load(sc, PropertiesLoader.ModelPath)

    val tweetsDF: DataFrame = loadSentiment140File(sc, PropertiesLoader.TestingFilePath)

    val actualVsPredictionRDD = tweetsDF.select("polarity", "status").rdd.map {

      case Row(polarity: Int, tweet: String) =>

        val tweetText = replaceNewLines(tweet)

        val tweetInWords: Seq[String] = ParsingTweets.getBarebonesTweetText(tweetText, stopWordsList.value)
        (polarity.toDouble, naiveBayesModel.predict(ParsingTweets.transformFeatures(tweetInWords)), tweetText)
    }

    val actualVsPredictionRDD2 = tweetsDF.select("polarity", "status").rdd.map {
      case Row(polarity: Int, tweet: String) =>
        val tweetText = replaceNewLines(tweet)
        (polarity.toDouble, CoreNLPSentimentAnalyzer.computeWeightedSentiment(tweetText), tweetText)
    }

    actualVsPredictionRDD.cache()

    val accuracy = 100.0 * actualVsPredictionRDD.filter(x => x._1 == x._2).count() / tweetsDF.count()
    val predictedInCorrect = actualVsPredictionRDD.filter(x => x._1 != x._2).count()

    println(f"""\n\t<==******** Prediction Accuracy compared to actual: $accuracy%.2f%% ********==>\n""")
    println(f"""\n\t<==********  Incorrect Values Predicted : $predictedInCorrect       ********==>\n""")

    val accuracy2 = 100.0 * actualVsPredictionRDD2.filter(x => x._1 == x._2).count() / tweetsDF.count()
    println(f"""\n\t<==******** Prediction Accuracy CoreNLP to actual: $accuracy2%.2f%% ********==>\n""")
    //saveAccuracy(sc, actualVsPredictionRDD)
  }

  /**
    * Loads the Sentiment140 file from the specified path using SparkContext.
    *
    * @param sc                   -- Spark Context.
    * @param sentiment140FilePath -- Absolute file path of Sentiment140.
    * @return -- Spark DataFrame of the Sentiment file with the tweet text and its polarity.
    */
  def loadSentiment140File(sc: SparkContext, sentiment140FilePath: String): DataFrame = {

    val sqlContext = SQLContextSingleton.getInstance(sc)

    val tweetsDF = sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", "false")
      .option("inferSchema", "true")
      .load(sentiment140FilePath)
      .toDF("polarity", "id", "date", "query", "user", "status")

    println(f"""\n\t<==********  Number of Rows in the Dataset  : ${tweetsDF.count()} ********==>\n""")

    // Drop the columns we are not interested in.
    tweetsDF.drop("id").drop("date").drop("query").drop("user")
  }

  /**
    * Saves the accuracy computation of the ML library.
    * The columns are actual polarity as per the dataset, computed polarity with MLlib and the tweet text.
    *
    * @param sc                    -- Spark Context.
    * @param actualVsPredictionRDD -- RDD of polarity of a tweet in dataset and MLlib computed polarity.
    */
  def saveAccuracy(sc: SparkContext, actualVsPredictionRDD: RDD[(Double, Double, String)]): Unit = {

    val sqlContext = SQLContextSingleton.getInstance(sc)
    import sqlContext.implicits._

    val actualVsPredictionDF = actualVsPredictionRDD.toDF("Actual", "Predicted", "Text")

    actualVsPredictionDF.coalesce(1).write
      .format("com.databricks.spark.csv")
      .option("header", "true")
      .option("delimiter", ",")
      // Compression codec to compress while saving to file.
      //.option("codec", classOf[GzipCodec].getCanonicalName)
      .mode(SaveMode.Append)
      .save(PropertiesLoader.ResultsPath)

  }
}
