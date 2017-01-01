/**
  * Created by dan.dixey on 14/10/2016.
  */

// Project Details
lazy val TwitterSparkSentiment = (project in file("app")).
  settings(
    name := "TwitterSparkSentiment",
    version := "1.0",
    scalaVersion := "2.11.8"
  ).
  settings(
    mainClass in assembly := Some("com.SentimentSpark.KafkaConsumer"),
    assemblyJarName in assembly := "SparkOutput.jar"
  )

// Versions
scalaVersion := "2.11.8"
val sparkVersion = "2.0.1"
val sparkCsvVersion = "1.4.0"
val configVersion = "1.3.0"
val jacksonVersion = "2.8.3"
val coreNlpVersion = "3.6.0"
val jedisVersion = "2.9.0"
val kafkaVersion = "0.8.2.1"
val scalaTest = "3.0.0"
val twitter4j = "4.0.5"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

libraryDependencies ++= Seq(
  // Configuration Files
  "com.typesafe" % "config" % configVersion,

  // Apache Spark
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.spark" %% "spark-hive" % sparkVersion,
  "org.apache.spark" %% "spark-streaming-kafka-0-8" % sparkVersion,
  "org.apache.avro" % "avro" % "1.8.1",

  // Kafka
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "org.apache.kafka" %% "kafka" % kafkaVersion,
  //"info.batey.kafka" % "kafka-unit" % "0.2",

  // Writing Tests - NOT DONE
  "org.scalatest" %% "scalatest" % scalaTest % "test",

  // Core-NLP Text Processing Library
  "edu.stanford.nlp" % "stanford-corenlp" % coreNlpVersion,
  "edu.stanford.nlp" % "stanford-corenlp" % coreNlpVersion classifier "models",

  // Twitter4j
  "org.twitter4j" % "twitter4j-stream" % "4.0.5",

  // Jackson
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion

)

lazy val defaultSettings = Defaults.coreDefaultSettings ++ Seq(
  resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
)

retrieveManaged := false
