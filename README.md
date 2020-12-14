# metering-data-Service

As its name suggests, this service is responsible for processing profile fractions data and meter readings and provide consumption for a given meter for a specific period.
The application exposes a REST API. It should contain following endpoints:

  - POST /v1/bulk-energy-consumption/meter-readings - Process the meter readings for more than 1 meter in a single call
  - POST /v1/bulk-energy-consumption/profile-fractions - Process the profile fractions data for more than 1 profile in a single call
  - GET /v1/energy-consumption/{meterId}/{month}/consumption - Gets the energy consumption for a given meter for a specific month
  - POST /v1/energy-consumption/{meterId}/meter-readings - Process the meter readings for ONLY 1 meter
  - POST /v1/energy-consumption/{profile}/fractions - Process the profile fraction for ONLY 1 profile
  
  Apart from the above mentioned endpoints, the service can also process the old format csv files  - check the section 'Process old CSV files' below for details
  
#### Prerequisites
 - mvn
 
#### Build
 - Run `mvn clean install` in the `metering-data` directory where the pom.xml resides.
 - A jar will be created in the target folder `metering-data/target`.
 
#### Deployment
 - Go to the target folder `metering-data/target` and execute below command
 - `java -jar metering-data-0.0.1-SNAPSHOT.jar`
 - Press ctrl + c in the same terminal window to shutdown the application
 

#### Testing
Deployed application can be accessed here [metering-data-service](http://localhost:8080/swagger-ui.html)
The in-memory h2 database can be accessed here: http://localhost:8080/h2-console/
* JDBC URL: jdbc:h2:mem:db
* User Name: sa
* Password: (blank)

For testing the API : 

* Click on POST /v1/energy-consumption/{profile}/fractions
* Click on Try it out.
* Execute the test scenarios mentioned in the file /metering-data/src/test/resources/SingleProfileFractionsTest

* Click on POST /v1/energy-consumption/{meterId}/meter-readings
* Click on Try it out.
* Execute the test scenarios mentioned in the file /metering-data/src/test/resources/SingleProfileMeterReadingsTest

* Click on GET /v1/energy-consumption/{meterId}/{month}/consumption
* Click on Try it out.
* Enter the appropriate values for Meter Id and month and check the output.

* Click on POST /v1/bulk-energy-consumption/profile-fractions
* Click on Try it out.
* Execute the test scenarios mentioned in the file /metering-data/src/test/resources/SingleProfileFractionsTest

* Click on POST /v1/bulk-energy-consumption/meter-readings
* Click on Try it out.
* Execute the test scenarios mentioned in the file /metering-data/src/test/resources/SingleProfileMeterReadingsTest

* Click on GET /v1/energy-consumption/{meterId}/{month}/consumption
* Click on Try it out.
* Enter the appropriate values for Meter Id and month and check the output.


#### Process old CSV files
* Old format files are kept here: 
	- /metering-data/src/main/resources/fractions.csv
	- /metering-data/src/main/resources/readings.csv
Please note the month format is a little different. Also, for the files to process the names should start with 'fractions' and 'readings' respectively.

* These files need to be kept in /metering-data/src/main/resources/legacyFileProcessing for processing
* They will be deleted if processed successfully
* In case of errors, a new file will be created with the same name but with 'log' extension in the same folder.

#### First level architecture design and assumptions:
* The application is using h2 in-memory database
* The provided solutions takes into account all the mentioned assumptions in the code challenge
* For real systems, the code will be distributed in different layers like validators, mappers etc.
* For real systems, logs should be written in logs files.
* The processing of old csv format should be done in a separate service
* Test cases for the the service ProcessCsvDataService have not been written because of time constraints.
* For real systems, integration tests should also be written
* This service should ideally be divided into multiple micro-services which should interact with each other.

