## Set up a Kafka Cluster with 3 brokers

- Review [yaml file ](docker-compose-brokers.yaml)
- Run docker command and this will spin up a kafka cluster with 3 brokers running in background.
```
docker-compose -f docker-compose-brokers.yml up -d 
```
- Write across the three Kafka brokers that we have part of the cluster. Create topic with the replication factor as 3
```
docker exec --interactive --tty kafka_1  \
kafka-topics --bootstrap-server kafka_1:19092 \
             --create \
             --topic test-topic \
             --replication-factor 3 --partitions 3
```
