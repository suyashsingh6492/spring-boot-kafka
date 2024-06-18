# Kafka running on Local using docker
## Set up kafka broker and zookeeper
- Navigate to the parent path of this project. you will find **docker-compose.yml** . run the below command.
```
docker-compose up
```
## Producer and Consume the Messages

- Let's going to the kafka container by running the below docker command.
```
docker exec -it kafka_demo bash
```
- Create a Kafka topic using the **kafka-topics** command.
  - **kafka_demo:19092** refers to the **KAFKA_ADVERTISED_LISTENERS** in the docker-compose.yml file.
  - refer **localhost:19092** if you are running kafka command outside of the container 

```
kafka-topics --bootstrap-server kafka_demo:19092 \
             --create \
             --topic test-topic \
             --replication-factor 1 --partitions 1
```
  - type **exit** to exit from kafka container

- Produce and Consume Messages to the topic from console producer and console consumer.
  -  Open a new terminal, Logged into the container and then launch a console producer and produce Messages to the topic named test-topic.
  -  ```
     docker exec --interactive --tty kafka_demo  \
     kafka-console-producer --bootstrap-server kafka_demo:19092 \
                       --topic test-topic
     ```
  - Open a new terminal, Consume Messages from the topic named test-topic from beginning.
  - ```
    docker exec --interactive --tty kafka_demo  \
    kafka-console-consumer --bootstrap-server kafka_demo:19092 \
                       --topic test-topic \
                       --from-beginning
    ```
 - Ctrl + c , to exit from the process. 

## Producer and Consume the Messages With Key and Value

- Produce Messages with Key and Value to the topic. key separator is - , send message like : g-GitHub , k-Kafka , k-Kibana
- If you send key without - , then you will get exception *No key separator found on line* and process exit. This is a limitation when it comes to console producer.
```
docker exec --interactive --tty kafka_demo  \
kafka-console-producer --bootstrap-server kafka_demo:19092 \
                       --topic test-topic \
                       --property "key.separator=-" --property "parse.key=true"
```
- Consuming messages with Key and Value from a topic. Print the key and value too. It will print : g - Github , g - Google

```
docker exec --interactive --tty kafka_demo  \
kafka-console-consumer --bootstrap-server kafka_demo:19092 \
                       --topic test-topic \
                       --from-beginning \
                       --property "key.separator= - " --property "print.key=true"
```

## Consume Messages using Consumer Groups
- consumer will be identified by group id, so pass --group <groupId> . we are going to be reading only the new messages. create this on 2 console and create min 2 partition . 
  - Command to describe a specific Kafka topic.
  - ```
    docker exec --interactive --tty kafka_demo  \
    kafka-topics --bootstrap-server kafka_demo:19092 --describe \
    --topic test-topic
    ```
  -  Alter topic Partitions to 2 , partition 0 and partition 1
  -  ```
      docker exec --interactive --tty kafka_demo  \
      kafka-topics --bootstrap-server kafka_demo:19092 \
      --alter --topic test-topic --partitions 2
     ```
  - Run consumer on 2 different console using same command, data will come in round - robin sequence , ex: a-abc , 1-one, b-bcd , 2-two 
  - ```
    docker exec --interactive --tty kafka_demo  \
    kafka-console-consumer --bootstrap-server kafka_demo:19092 \
    --topic test-topic --group console-consumer-1\
    --property "key.separator= - " --property "print.key=true"

### List the topics in a cluster 

- List all the different topics that are part of the Kafka cluster ( *__consumer_offsets is a topic which actually gets created automatically by the Kafka cluster to maintain the consumer offsets. *)
  
```
docker exec --interactive --tty kafka_demo  \
kafka-topics --bootstrap-server kafka_demo:19092 --list

```

### How to view consumer groups
-
```
docker exec --interactive --tty kafka_demo  \
kafka-consumer-groups --bootstrap-server kafka_demo:19092 --list
```


## Log file and related config

- Log into the kafka container.
```
docker exec -it kafka_demo bash
```
- The config file is present in the  ** /etc/kafka/server.properties ** path.
- The log file is present in the ** /var/lib/kafka/data/ ** path.


