# Anomaly Detection For Sampling in Distributed Tracing System 

The Following repository consists of 4 folders, each folder is part of the implementation of the thesis "Anomaly Detection For Sampling in Distributed Tracing System"

### Running The Parking Spot Finder Service

5 Spring-Boot Applications

Setup 
```
cd {workspace}/AnomalyDetectionForSampling/Parking Spot Finder Service
```
Running the Individual Services

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

End with an example of getting some data out of the system or using it for a little demo

## Running the tests

Explain how to run the automated tests for this system

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Explain what these tests test and why

```
Give an example
```

## Deployment

Add additional notes about how to deploy this on a live system

## Built With

* [Dropwizard](http://www.dropwizard.io/1.0.2/docs/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
* [ROME](https://rometools.github.io/rome/) - Used to generate RSS Feeds

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Billie Thompson** - *Initial work* - [PurpleBooth](https://github.com/PurpleBooth)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone whose code was used
* Inspiration
* etc
