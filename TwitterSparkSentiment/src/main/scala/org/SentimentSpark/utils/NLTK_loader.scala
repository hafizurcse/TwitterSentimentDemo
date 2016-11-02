package org.SentimentSpark.utils

import org.SentimentSpark.utils.LogUtils._

import scala.io.Source

/**
  * Created by dan.dixey on 13/10/2016.
  * Helper class for loading stop words from a file ["NLTK_English_Stopwords_Corpus.txt"] from the classpath.
  * NLTK - Natural Language Toolkit
  */
object NLTK_loader {

  log.info("Loading NLTK File")

  /**
    * Setting the logging level for the project
    *
    * @param stopWordsFileName -- File Path Name of the stopwords text file
    */
  def loadStopWords(stopWordsFileName: String): List[String] = {
    Source.fromInputStream(getClass.getResourceAsStream("/" + stopWordsFileName)).getLines().toList
  }
}
