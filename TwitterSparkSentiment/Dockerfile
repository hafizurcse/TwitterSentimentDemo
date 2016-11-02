
# WIP : Need to compile and Test and Upload to DockerHub

#
#  Created by dan.dixey on 27/10/2016.
#

# Dockerfile for Wrapping the JAR file up
FROM java:jre-alpine
MAINTAINER dandxy89
ADD target/TwitterKafkaProducer-assembly-1.0.jar /TwitterKafkaProducer-assembly-1.0.jar
CMD ["java", "-jar", "TwitterKafkaProducer-assembly-1.0.jar"]
