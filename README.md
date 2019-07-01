# Anomaly Detection For Sampling in Distributed Tracing System 

The Following repository consists of 4 folders, each folder is part of the implementation of the thesis "Anomaly Detection For Sampling in Distributed Tracing System"

## Parking Spot Finder Services

5 Spring-Boot Applications

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

For training the model use 
```
http://localhost:8085/train
```
For prediction with a json payload use
```
http://localhost:8085/predict
```
```
The payload should be something like this
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
## The Jaeger Prototype 

It can be ran by executing the command. (specifiy the log4j2 file with the help of -Dlog4j.configurationFile)

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

![](gifs/agent.gif)

## Scripts. 

There are two scripts one for ingestion and the other for editing the JSON dataset.

### Ingestion Script

The Ingestion scripts can be run by simply executing 

```
sudo sh ingestData.sh
```
this will start sending requests to Parking Spot Finder services

### EditJSON Script

This python scripts increase the number of JSON enteries and also insert anamolies in them. It creates a new file with a larger datset and the anomalies inserted in them, which are specified in the script itself.

For running use
```
python3 editJson.py
```



