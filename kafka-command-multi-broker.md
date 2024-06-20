## Set up a Kafka Cluster with 3 brokers

- Review [yaml file ](docker-compose-brokers.yaml)
- Run docker command and this will spin up a kafka cluster with 3 brokers running in background.
```
docker-compose -f docker-compose-brokers.yaml up -d 
```
- Write across the three Kafka brokers that we have part of the cluster. Create topic with the replication factor as 3. we are going to write across the three Kafka brokers that we have part of the cluster and having 3 partition. If you have lesser broker , you will get exception after runing this command.
- Even though I'm just providing the value as kafka_1:19092,  still, the Kafka topic is going to be expanded across all the Kafka brokers that are part of the cluster. 
```
docker exec --interactive --tty kafka_1  \
kafka-topics --bootstrap-server kafka_1:19092 \
             --create \
             --topic test-topic \
             --replication-factor 3 --partitions 3
```
- Produce Messages to the topic. We are login into the kafka_1 thats why we pass localhost:9092 . 
```
docker exec --interactive --tty kafka_1  \
kafka-console-producer --bootstrap-server localhost:9092,kafka_2:19093,kafka_3:19094 \
                       --topic test-topic
```
- Consume Messages from the topic from the beginning.
```
docker exec --interactive --tty kafka_1  \
kafka-console-consumer --bootstrap-server localhost:9092,kafka_2:19093,kafka_3:19094 \
                       --topic test-topic \
                       --from-beginning
```

### Setting up min.insync.replica

- Topic - test-topic , minimum in sync replica that's required for this test-topic is going to be 2. if no of broker is less then 2 and if published the messaged in test-topic. We ran into an error NOT_ENOUGH_REPLICAS.

```
docker exec --interactive --tty kafka_1  \
kafka-configs  --bootstrap-server localhost:9092 --entity-type topics --entity-name test-topic \
--alter --add-config min.insync.replicas=2
```
