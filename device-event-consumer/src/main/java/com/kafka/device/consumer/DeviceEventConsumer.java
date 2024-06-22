package com.kafka.device.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafka.device.service.DeviceEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.stereotype.Component;

//@Component  //create manual offset for testing
@Slf4j
public class DeviceEventConsumer {

    @Autowired
    private DeviceEventService deviceEventService;

    //It is going to give you a consumer record., group-id is mandatory field
    @KafkaListener(topics = {"device-events"})
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        //So the Kafka listener container, which is going to pull the records, it is going to get multiple records
        //at the same time, but it is going to pass the records one by one to this Onmessage So we have the consumer
        //ready now.
        log.info("Consumer Record: {} ", consumerRecord);
        //We have 3 partitions assigned. And we have one single instance which is actually pulling all the three partitions.
        //check the logs headers = RecordHeaders( the Kafka headers, we are passing the key as event source, but the value is kind of encrypted.
        //becuase we are passing byte in value in header

        deviceEventService.processDeviceEvent(consumerRecord);
    }

    //check the KafkaAutoConfiguration  --check in kafka lib, auto config lib , or copy it delete the content
    //EnableConfigurationProperties KafkaProperties  class has information about the producer configuration properties and consumer
    // configuration properties.  go to our application dot Yaml file ap servers, value Deserializer and
    // group id,
    //KafkaAnnotationDrivenConfiguration first condition for this class is at condition on clause enable Kafka clause
    //EnableKafka So basically it looks for this annotation.
    // If that annotation is enabled, then it's going to basically enable this auto configuration.
    //KafkaAnnotationDrivenConfiguration has 3 beans kafkaListenerContainerFactory. kafkaListenerContainerFactoryConfigurer
    // kafkaListenerContainerFactory kafka uses
    //  @KafkaListener(topics = {"device-events"},,containerFactory = ) if not define automatically taken by spring boot
    // Kafka Consumer factory. This bean is defined in the Kafka auto configuration.Java class.
    //And then this is going to read all the consumer properties from here.
    //buildConsumerProperties > read property and build consumer property > buildCommonProperty, buildProperty(),
    //kafkaListenerContainerFactory method return concurrentKafkaListenerContainerFactory
}
