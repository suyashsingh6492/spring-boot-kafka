package com.kafka.device.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafka.device.service.DeviceEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeviceEventRetryConsumerNetworkFailure {

    @Autowired
    private DeviceEventService deviceEventService;

    //you build multiple listeners, the group ID that you provide here is not going to work out of the box.
// So it's always recommended to add a group ID over here.
    @KafkaListener(topics = {"${topics.retry}"}
            ,groupId = "retry-listener-group")
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        log.info("Consumer Record in Retry Topic : {} ", consumerRecord);
        consumerRecord.headers()
                .forEach(header -> {
                    log.info("Header key : {} , value : {} ",header.key(),new String(header.value())); //value are in bytes
                });
    }

}
