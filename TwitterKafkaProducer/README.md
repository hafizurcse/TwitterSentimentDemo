### Kafka Producer to Push Twitter Data (Part 1)

*Dan Dixey*

*Sunday 9th October*

*Applied use of Scala using Akka, Twitter4j and Kafka*

The purpose of this project is to pull tweets from twitter and push into Kafka topic.

### Pre-Requisites for this project

1. [Download](http://kafka.apache.org/downloads.html) the 0.8.2.1 version, unzip it and make (if required).

2.  Run the following command to Start zookeeper & Kafka:
    
         $ bin/zookeeper-server-start.sh config/zookeeper.properties 
         $ bin/kafka-server-start.sh config/server.properties

3.  This project has been built using sbt assembly, so a .jar file has been generated that you can run with a one-liner. To run this execute the following command:

        $ cd TwitterKafkaProducer
        $ java -jar target/scala-2.11/TwitterKafkaProducer-assembly-1.0.jar
        
### Filtering currently and Kafka Queue Name

Filters:

      twitterStream.sample("en")
      twitterStream.filter("data", "analytics", "brexit", "football", "trump", "rooney", "clinton", "russia", "usa",
                           "python", "scala", "qbiz", "qgroup", "lloyds", "gsk", "oil", "pharma")
                           
Topic Name:
                           
      ScalaTwitter
                           
### Re-building the .jar file

It is likely that may want to change or amend the filtering items. If you would like to use the following commands to do so.

1. Open IntelliJ (add the Scala Plugin for Scala support)

2. Navigate to and open the file:

    TwitterKafkaProducer/src/main/scala/com.QbizTwitterProducer/Util.scala
    
3. Edit filtering items

4. Exit IntelliJ and navigate to the Twitter4jKafka folder

5. Run the following command:

    $ sbt assembly
    
*The output is shared within the target/scala-2.11/ folder*
