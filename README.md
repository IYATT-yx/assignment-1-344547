# Weather Sync

A System for Synchronizing and Aggregating Weather Data from Multiple Sources。

## system structure

![project structure](README/project%20structure.png)

![system structure](README/system%20structure.png)

### JsonParser

JsonParser is based on Google's [Gson](https://github.com/google/gson) implementation.JsonParser provides a set of methods for parsing and processing JSON data, including reading key value pairs from text files and converting them into JSON objects, converting JSON objects into strings, converting JSON strings into JSON objects, formatting JSON objects into readable string representations, writing multiple JSON strings to files, reading JSON string arrays from files, and extracting JSON fields from GET request content, And write and read hash maps of aggregated request data into JSON files. These methods can conveniently parse, transform, and store JSON data.  

### Feed

Feed implements an aggregated data source that can receive requests from content servers and aggregate them together. It provides functions such as adding requests, setting expired tasks, and obtaining text representations of recent requests. By using timers and hash maps to manage requests and expiration tasks, aggregation and expiration processing of requests are achieved. Aggregated data sources can sort recent requests according to certain rules and return text representations to effectively display and maintain aggregated data.  

### HandlerThread

HandlerThread implements the main function of an aggregation server, which can receive requests from multiple content servers and aggregate them together. By using priority blocking queues and threads to process requests, the ability for concurrent processing is achieved. Aggregation servers provide the ability to obtain textual representations of recent requests, add requests, and process different request types to effectively manage and respond to requests from content servers.  

### Request

Request is mainly used to handle requests and responses between aggregation servers, content servers, and clients, and provides methods to parse and process various fields of requests and responses. It also provides some auxiliary methods, such as creating URL objects and cloning objects.  

### LamportClock

Lamport clock implements the basic operations of the Lamport logical clock algorithm, including obtaining and increasing clock values, comparing and setting clock values, and providing methods for obtaining the current clock value. By using this code, global sorting and concurrency control of events can be achieved in distributed systems.

### GlobalConstant

The GlobalConstant class contains constant definitions used in the application.  

## usage

### environment

OS: Ubuntu 22.04 x86_64  
Java: openjdk-8-jdk 8u382-ga-1~22.04.1  

GNU Make 4.3

### compile

```bash
cd test
make build
```

### run

run AggregationServer
```bash
java -classpath .::../lib/gson-2.10.1.jar AggregationServer [port]
```
If no `[port]` is specified, 4567 will be set by default.  
example:
```bash
java -classpath .::../lib/gson-2.10.1.jar AggregationServer 4567
```
  
---

run ContentServer
```bash
java -classpath .:../lib/gson-2.10.1.jar ContentServer [AggregationServer IP]:[AggregationServer port] [id] [weather file]
```
`[AggregationServer IP]:[AggregationServer port]` is the address of the aggregation server to connect to.  
`[id]` is the identifier used to distinguish clients.  
`[weather file]` is the weather data file to be uploaded to the aggregation server.  
example:
```bash
java -classpath .:../lib/gson-2.10.1.jar ContentServer 127.0.0.1:4567 ContentServer1 ./resources/weather1.txt
```

---

run GETClient
```bash
java -classpath .:../lib/gson-2.10.1.jar GETClient [AggregationServer IP]:[AggregationServer port] [output file]
```
`[output file]`is the weather data requested by GETClient from the aggregation server, formatted and saved as a JSON file.  

```bash
java -classpath .:../lib/gson-2.10.1.jar GETClient 127.0.0.1:4567 "./resources/client_out.json"
```

### test

The manual operation method is explained above, and automatic test scripts are also provided to verify the operation of the entire system.The testing flowchart is shown in the figure.  

![test flowchart](README/test%20flowchart.png)

The test script will first start the aggregation server, then run 5 content servers in sequence, and then run the client to request data from the aggregation server. After requesting data, the test script will compare the requested data with all data uploaded by the content servers to verify the support for concurrent connections on the content server and the effectiveness of the lamport clock. Afterwards, the test script will shut down the aggregation server and all content servers, attempt to resume the operation of the aggregation server, and then use the client to request data from the aggregation server and verify the data content to test the recovery of the aggregation server to the state before the interruption in the event of an abnormal shutdown. After that, the test script will wait for 30 seconds and then have the client request data from the aggregation server. If the data is empty, verify that when the content server disconnects and exceeds the set time, the aggregation server will clear the expired data.

start test
```bash
bash test.sh
```





