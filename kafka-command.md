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



