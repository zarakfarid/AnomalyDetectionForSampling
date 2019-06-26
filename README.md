# Anomaly Detection For Sampling in Distributed Tracing System 

The Following repository consists of 4 folders, each folder is part of the implementation of the thesis "Anomaly Detection For Sampling in Distributed Tracing System"

### Running The Parking Spot Finder Service

5 Spring-Boot Applications

# Setup 
```
cd {workspace}/AnomalyDetectionForSampling/Parking Spot Finder Service
```
## Running the Individual Services

```
//For Root-Service: Parking Spot Finder Service
mvn -pl park-spot-service -am spring-boot:run
//For Authentication Service
mvn -pl authentication-service -am spring-boot:run
//For Verification Service
mvn -pl verification-service -am spring-boot:run
//For Payment Service
mvn -pl payment-service -am spring-boot:run
//For Spot-Finder Service
mvn -pl spot-finder-service -am spring-boot:run
```

## The Spark Application is integrated with a spring boot app aswell, to provide a service layer, so that the user can train and predict using simple http requests. 

It can be run both way For Spring-Boot
```
clean spring-boot:run
```
For actuall Spark deployemnt use "spark submit", first create a Fat-Jar by running
```
mvn clean package
```
and then submit the application with basic spark commands

## The Jaeger Prototype can be run simply running the command. 

```
cd {workspace}AnomalyDetectionForSampling/Jaeger Prototype Agent
mvn exec:java
```
## Scripts. 

```
cd {workspace}AnomalyDetectionForSampling/Jaeger Prototype Agent
mvn exec:java
```


