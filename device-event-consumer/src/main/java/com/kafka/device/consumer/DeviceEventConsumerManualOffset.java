package com.kafka.device.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafka.device.service.DeviceEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeviceEventConsumerManualOffset implements AcknowledgingMessageListener<Integer, String> {
    //because here when you are trying to manually commit the offset, the message listener is going to be of different type.

    @Autowired
    private DeviceEventService deviceEventService;


//    @KafkaListener(topics = {"device-events"})
//    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) {
//
//        log.info("Consumer Record: {} ", consumerRecord);
//
//    }

    //you need to provide that Kafka listener still, but you are going to commit the offset in a different approach.
    @Override
    @KafkaListener(topics = {"device-events"},groupId = "device-events-listener-group")
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord, Acknowledgment acknowledgment) {
        log.info("Consumer Record: {} ", consumerRecord);

        try {
            deviceEventService.processDeviceEvent(consumerRecord);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        acknowledgment.acknowledge();//By doing this, you are letting the message listener know that you have successfully processed the message.

    }

}
