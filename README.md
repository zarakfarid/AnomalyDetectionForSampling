# Anomaly Detection For Sampling in Distributed Tracing System 

The Following repository consists of 4 folders, each folder is part of the implementation of the thesis "Anomaly Detection For Sampling in Distributed Tracing System"

## Getting Started

Download and run Elastic Search from `https://www.elastic.co/products/elasticsearch` 

Download Jaeger from `https://www.jaegertracing.io/docs/1.12/getting-started/` and run Jaeger Collector configured with elastic search, using command

```
./jaeger-collector --span-storage.type elasticsearch
```
and run Jaeger Query configured with elastic search
```
./jaeger-query --span-storage.type elasticsearch
```
If your system is not deployed on localhost, you can specify the hostname and port

## Parking Spot Finder Services

Run these 5 Spring-Boot Applications for the Use-case

```
cd {workspace}/AnomalyDetectionForSampling/Parking Spot Finder Service
```
### Running the Individual Services

```
//For Root-Service: Parking Spot Finder Service on port 8080
mvn -pl park-spot-service -am spring-boot:run
```
```
//For Authentication Service on port 8081
mvn -pl authentication-service -am spring-boot:run
```
```
//For Verification Service on port 8082
mvn -pl verification-service -am spring-boot:run
```
```
//For Payment Service on port 8084
mvn -pl payment-service -am spring-boot:run
```
```
//For Spot-Finder Service on port 8083
mvn -pl spot-finder-service -am spring-boot:run
```
## The Jaeger Prototype 

It can be run by executing the command. (specifiy the log4j2 file with the help of -Dlog4j.configurationFile)

```
cd {workspace}AnomalyDetectionForSampling/Jaeger Prototype Agent
```
```
mvn exec:java
```
for checing out if the prototype is running you can use
```
lsof -i -n -P | grep UDP
```
The agent will start listening to UDP packets from port specified in the code, for now "localhost:6831" and will send data to the collector using HTTP requests, URL: http://localhost:14268/api/traces also specified in the code.

![](gifs/agent.gif)

There is a variable in org.thrift.agent.server.handler.InMemorySpanServerHandler called **flushEverything**, if this is true all trace data will be forwarded to the collector without the MLFilter, this is used in-order to gather trace data for training the ML Model.

## The Spark Application

It is integrated with a spring boot app aswell, to provide a service layer, so that the user can train and predict using simple http requests. For running it as a spring boot application use
```
clean spring-boot:run
```
For actuall Spark deployemnt using "spark submit", first comment-out the spring-boot plugin in the pom file and then uncomment the **shade plugin (already present in pom)**. The application was tested with Spark 2.3.2, if there are any dependency conflicts with your spark instance, please use the shade plugin to repackage them, it will solve all the dependency issues. After that create a Fat-Jar by running the command. 
```
mvn clean package
```
and then submit the application with spark command
```
spark-submit  parkingSpot-predictor-0.0.1-SNAPSHOT.jar
```
For training the model use (make sure you have some trace data available in elastic-search)
```
http://localhost:8085/train
```
For prediction with a json payload use
```
http://localhost:8085/predict
```
```
The payload should be like this
[
  {
    "CreditCard": 6772319276427601,
    "Position": "-79.75866, -80.33546",
    "Address": "179-8071 Consectetuer Avenue",
    "Money": "367,42$",
    "Username": "Kristina UNION SELECT 1,@@version-- ",
    "Password": "ANnX4thxArQGNkb3"
  }
]
```

## Scripts. 

There are two scripts one for ingestion and the other for editing the JSON dataset.

### Ingestion Script

The Ingestion scripts can be run by simply executing 

```
sudo sh ingestData.sh
```
this will start sending requests to Parking Spot Finder services

### EditJSON Script

This python scripts increases the number of JSON enteries and also insert anamolies in them. It creates a new file with a larger datset and the anomalies inserted in them. The anomalies are specified in the script itself.

For running use
```
python3 editJson.py
```