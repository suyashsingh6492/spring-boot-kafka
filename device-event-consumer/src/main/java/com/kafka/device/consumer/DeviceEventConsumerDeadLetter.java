package com.kafka.device.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeviceEventConsumerDeadLetter {


    @KafkaListener(topics = {"device-events.DLT"}, groupId = "dead-letter-listener-group")
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        log.info("Consumer Record in Dead Letter DLT Topic : {} ", consumerRecord);

    }

}
