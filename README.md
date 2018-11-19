# tcp-server

### Simple TCP server which provides following functionalities
* Listens for up to 5 concurrent connections on port 4000
* Processes 9 digit input followed by a newline sequence
* Immediately disconnects a client connection if input does not meet the 9 digit validation
* If the word *terminate* is sent to the server, the application will attempt to shutdown gracefully
* The unique 9 digit numbers received are written to a file called *numbers.log*
* The *numbers.log* file is recreated when application starts up

## Prerequisites
- JDK 1.8+
- Gradle 4.8

### How to Run?
The `gradlew` executable can be used to build the project without needing to install Gradle.

```
gradle clean build OR ./gradlew clean build

java -jar build/libs/tcp-server-1.0.0-SNAPSHOT.jar
```

## Design
#### Classes
MetricsWorker -  a single Runnable instance that prints the data metrics every 10 seconds

ProcessWorker -  worker thread (Runnable) that processes data from incoming client connections to TCP server (up to 5 instances at a time)

TCPServerApplication - Creates two instances of ExecutorService (one fixed thread pool set at 5 AND a scheduled thread pool set at 1). Main thread listens for incoming TCP
connections and instantiates a new ProcessWorker if possible, otherwise it will queue the work internally until a new thread can be instantiated 

#### Data Structures
A ConcurrentHashSet is created from a ConcurrentHashMap implementation. The set holds all the unique 9 digit input that are processed by the worker threads.
The size of the set is leveraged by the MetricsWorker to indicate the total of unique numbers received. 

Two AtomicInteger are also used to keep track of the count of duplicates and unique numbers received within the most recent 10 second interval. After each interval, the counts are
printed by MetricsWorker and reset to 0.

## Testing
Load testing was done externally using JMeter to confirm 2M+ numbers could be received. There is also a shell script *test/resources/tcp-data-test-script.sh* which sends
TCP traffic to the server. However, it's only single threaded so it can be used as a smoke test to verify the basic functionality.

In order to run the test method *simulateLoadTest* in TCPServerApplicationTest, the TCPServerApplication should be started. The intention was to create 250 threads and within each thread, loop
through potentially unique 9 digit numbers to send TCP traffic to the server.
