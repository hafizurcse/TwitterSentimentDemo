### Kafka consumer to analyse tweets through Spark (Part 2)

*Dan Dixey*

*Thursday 27th October*

*Applied use of Scala using Spark, Twitter4j and Kafka*

The purpose of this project is to pull tweets from twitter and push into Kafka topic.

### Pre-Requisites for this project 

Part 1 and 2 should already be running but if not.

1. [Download](http://kafka.apache.org/downloads.html) the 0.8.2.1 version, unzip it and make (if required).

2.  Run the following command to Start zookeeper & Kafka:
    
         $ bin/zookeeper-server-start.sh config/zookeeper.properties 
         $ bin/kafka-server-start.sh config/server.properties

3.  This project has been built using sbt assembly, so a .jar file has been generated that you can run with a one-liner. To run this execute the following command:

        $ cd TwitterKafkaProducer
        $ java -jar target/scala-2.11/TwitterSparkSentiment-assembly-1.0.jar
                           
### Re-building the .jar file

Shouldn't require rebuilding the .jar file to often after the first build. If it is required then take these steps:

4. Navigate to the TwitterSparkSentiment folder in your terminal

5. Run the following command:

    $ sbt -J-Xms4096m -J-Xmx4096m assembly # More memory

*The output is shared within the target/scala-2.11/ folder* 
 
### Training the Model

Requires the [Twitter Sentiment140 Dataset](https://docs.google.com/file/d/0B04GJPshIjmPRnZManQwWEdTZjg/edit)

1. Easiest method, open IntelliJ and run the **TrainModelsNB.scala** file.

## Monitoring the WebUI Topic

Once the twitter messages have been parsed they will be pushed into a separate KAFKA TOPIC called "WebUI"

To create a consumer to monitor the output run the follow command from the Kakfa folder:

```bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic WebUI```

The format of the message is in JSON format:

```
    {"tweet":"SOME MESSAGE",
     "coreNLPlabel":"Negative",
     "created_at":"Thu Dec 29 12:21:43 GMT 2016",
     "author_username":"SOME USER NAME",
     "MLLIBlabel":"Positive"}
```
