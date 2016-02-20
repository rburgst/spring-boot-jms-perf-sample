# Spring Boot JMS Sample

A very simple JMS test application which can also be used for measuring performance of JMS brokers.

Works with 

* ActiveMQ
* ArtemisMQ
* ApolloMQ
* HornetQ

The aim of this sample is to test performance, memory behaviour of a single producer sending into a single queue.
The queue is monitored by 2 consumers where each consumer filters for `CLIENT_ID=X` (where `X` = `1` or `2`).

The producer will send out packs of 10 messages for each consumer. I.e. 


* CLIENT_ID=1
* CLIENT_ID=1
* CLIENT_ID=1
* CLIENT_ID=1
* CLIENT_ID=1
* CLIENT_ID=1
* CLIENT_ID=1
* CLIENT_ID=1
* CLIENT_ID=1
* CLIENT_ID=1
* CLIENT_ID=2
* CLIENT_ID=2
* CLIENT_ID=2
* CLIENT_ID=2
* CLIENT_ID=2
* CLIENT_ID=2
* CLIENT_ID=2
* CLIENT_ID=2
* CLIENT_ID=2
* CLIENT_ID=2


The result should look like this


                                              .---- consumer 1 (CLIENT_ID=1)
                                             /
    producer   11111122222211111.. ->  queue1  
                                             \
                                              `---- consumer 2 (CLIENT_ID=2)
                                              
## Running the application

1. start the 2 receivers: `q1rec1.sh, q1rec2.sh`
2. wait until both of them are running
3. start the sender: `q1sender.sh`

For testing the performance with multiple queues and producers, you can repeat the same process for 
`q1rec1.sh, q1rec2.sh, q1sender.sh`.


### Configuration options

| Configuration | Description | Default value |
| ------------- | ------------|-------------- |
| `jms.queueName`| The name of the JMS queue to use | `queue1` |
| `jms.selector` | the JMS selector for which the receiver is listening, should be either  `CLIENT_ID=1` or `CLIENT_ID=2` | `CLIENT_ID=1`|
| `sleepTime`    | The delay in [ms] between each message | `0` (no wait) |

## Using different MQ

The main configuration for the samples is in `application.properties`.



### ActiveMQ

The `build.gradle` file needs to be changed in the following way:

* Remove (comment out) the `apache-artemis` dependency (if enabled)
  
        // compile("org.springframework.boot:spring-boot-starter-artemis")

* Enable 
        
        compile("org.apache.activemq:activemq-broker")


Should work with default settings, you might need to change the username/password configured

    spring.activemq.broker-url=tcp://127.0.0.1:61616
    spring.activemq.user=admin
    spring.activemq.password=admin

### ApolloMQ

* Use the same settings as for ActiveMQ in `build.gradle`
* Reconfigure the `application.properties`:

    spring.activemq.broker-url=tcp://127.0.0.1:61613
    spring.activemq.user=admin
    spring.activemq.password=password
    
### ArtemisMQ

* Remove (comment out) the `apache-activemq` dependency (if enabled)
  
        // compile("org.apache.activemq:activemq-broker")

* Enable 
        
        compile("org.springframework.boot:spring-boot-starter-artemis")

* Use the following settings in `application.properties`

        spring.artemis.mode=native
        spring.artemis.host=localhost
        spring.artemis.port=61616
        
        spring.artemis.embedded.enabled=false

### HornetQ

HornetQ is the original predecessor from Apache Artemis (it was handed over to the Apache Foundation), therefore
it is nearly exatly the same as `Apache Artemis`.

* comment out all dependencies for Apache ActiveMQ and Artemis
* add the following dependency:

        compile("org.springframework.boot:spring-boot-starter-hornetq")

* Add the following configuration to `application.properties`:

        spring.hornetq.mode=native
        spring.hornetq.host=localhost
        spring.hornetq.port=9876
        
        
## Specific Scenarios

#### Only consume half of the messages

It is quite instructive to test the case where there is 1 producer and 1 consumer but the consumer only collects
half of the messages. 
Depending on the broker configuration this will typically lead to producer back-pressure after the sending window 
is full, eventually the single consumer will no longer receive messages

## Credits

This sample is based on [https://spring.io/guides/gs/messaging-jms/](https://spring.io/guides/gs/messaging-jms/) ([https://github.com/spring-guides/gs-messaging-jms.git](https://github.com/spring-guides/gs-messaging-jms.git))

## References

* [http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-messaging.html](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-messaging.html)