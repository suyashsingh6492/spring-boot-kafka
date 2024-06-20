package com.kafka.device;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DeviceEventProducerApplication {
	/*
        docker exec --interactive --tty kafka_1  \
        kafka-topics --bootstrap-server kafka_1:19092 --list

        you'll have device-events in topic

        docker exec --interactive --tty kafka_1  \
        kafka-topics --bootstrap-server kafka_1:19092 --describe \
        --topic device-events

        to check replication factor

        to check consumer message
        docker exec --interactive --tty kafka_1  \
        kafka-console-consumer --bootstrap-server kafka_1:19092 \
                       --topic device-events \
                       --from-beginning



        */
	public static void main(String[] args) {
 	SpringApplication.run(DeviceEventProducerApplication.class, args);

	}

}
