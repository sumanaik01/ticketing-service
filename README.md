# Ticketing Service

This is a basic implementation of a ticketing service to book tickets in a Venue 

## Getting Started

Clone this project on your local and build it using 
```
mvn clean install
```

### Prerequisites

Please make sure you have these installed on your system:
Java 
Maven


### Assumptions
1. No backend DB has been used as per project requirement, all operations are being saved in memory in objects.
2. Per design of this service, the best available seats are from front to back and left to right, 
eg best available seat is leftmost seat in the first row.
3. Error message is returned to the user if consecutive seat allocation cannot be made in any of the rows
4. The code uses 5 seconds as default value for ON_HOLD timeout for ease of testing
5. Seat numbers start from 0 to N
7. Row numbers start from 0 to N
6. Please wait for the results as timeout has been added to test all scenarios run with all sdet of number for a clear visual



## Running the application

Once the project is cloned and built on your system, plesae follow the below instructions to run and test the application

The application requires 3 runtime arguments to be input
1. Rows in Venue
2. Seats in each row
3. OnHold timeout eg 5000 for 5 seconds. Lesser value is preffered to run the scenarios quickly.

If using eclipse, 
Import the project and open App.java
Right click and set the values for parameters under run config Arguments an run it as Java application


If running via commandline
Cd to project
Run as follows.Feel free to update the values of the arguments

```
mvn clean install
mvn exec:java -Dexec.mainClass="com.ticketing.service.util.App" -Dexec.args="5 5 5000"
```

### And coding style tests

mvn clean install executes a bunch of junit tests which run all of the scenarios covered by App.java


## Authors

* **Suma Naik** 


