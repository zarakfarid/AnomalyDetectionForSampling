# Anomaly Detection For Sampling in Distributed Tracing System 

The Following repository consists of 4 folders, each folder is part of the implementation of the thesis "Anomaly Detection For Sampling in Distributed Tracing System"

## Parking Spot Finder Services

5 Spring-Boot Applications

```
cd {workspace}/AnomalyDetectionForSampling/Parking Spot Finder Service
```
### Running the Individual Services

```
//For Root-Service: Parking Spot Finder Service
mvn -pl park-spot-service -am spring-boot:run
```
```
//For Authentication Service
mvn -pl authentication-service -am spring-boot:run
```
```
//For Verification Service
mvn -pl verification-service -am spring-boot:run
```
```
//For Payment Service
mvn -pl payment-service -am spring-boot:run
```
```
//For Spot-Finder Service
mvn -pl spot-finder-service -am spring-boot:run
```

## The Spark Application

It is integrated with a spring boot app aswell, to provide a service layer, so that the user can train and predict using simple http requests. It can be run both way, for running it as a spring boot application use
```
clean spring-boot:run
```
For actuall Spark deployemnt use "spark submit" by first create a Fat-Jar, running the command
```
mvn clean package
```
and then submit the application with basic spark commands

## The Jaeger Prototype 

It can be ran by executing the command. 

```
cd {workspace}AnomalyDetectionForSampling/Jaeger Prototype Agent
```
```
mvn exec:java
```
for checing out if the prototype is runnging you can use
```
lsof -i -n -P | grep UDP
```
The agent will start listening to UDP packets from port specified in the code, for now "localhost:6831" and will send data to the collector using HTTP requests, URL: http://localhost:14268/api/traces also specified in the code

## Scripts. 

There are two scripts one for ingestion and the other for editing the JSON dataset.

### Ingestion Script

The Ingestion scripts can be run by simply executing 

```
sudo sh ingestData.sh
```
this will start sending requests to Parking Spot Finder services

### EditJSON Script

This python scripts increase the number of JSON enteries and also insert anamolies in them. It creates a new file with a larger datset and the anomalies specified in the script itself.




