package com.kafka.device.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.device.domain.DeviceEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class DeviceEventProducer {

    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Value("${spring.kafka.topic}")
    public String topic;


    public DeviceEventProducer(KafkaTemplate<Integer, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<SendResult<Integer, String>> sendDeviceEvent(DeviceEvent deviceEvent)
            throws JsonProcessingException {
        String value = objectMapper.writeValueAsString(deviceEvent.device());

        Integer key = deviceEvent.deviceEventId();
        /*
        When this call is made for the very first time, there is a blocking call that happens.
        And what this call is for is to get the metadata about the Kafka cluster.
        If this call fails, actually we won't be able to send any message into the Kafka topic.
        once this call is successful. Number two is this is when the send message actually happens, send message happens, and this returns a completablefuture.

        remove the docker container docker-compose down --remove-orphans , remove all the data and container


        * */
        CompletableFuture<SendResult<Integer, String>> result
                = kafkaTemplate.send(topic, key, value);
        return result.whenComplete((sendResult, throwable) -> {
            if (throwable != null) { //so that means there is some exception that's been experienced.
                handleFailure(key, value, throwable);
            } else {
                handleSuccess(key, value, sendResult); //sendResult holds the information about which partition
                // this particular message is being sent to.
            }
        });
    }


    public SendResult<Integer, String> sendDeviceEvent_sync(DeviceEvent deviceEvent)
            throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        String value = objectMapper.writeValueAsString(deviceEvent.device());

        Integer key = deviceEvent.deviceEventId();
        //1. Blocking first call: to get metadata
        //2. Block and wait until the message to the kafka
        SendResult<Integer, String> sendResult = kafkaTemplate.send(topic, key, value)
                //.get();
                .get(3, TimeUnit.SECONDS);  //timeout of 3 secs
        handleSuccess(key, value, sendResult);
        return sendResult;
    }


    public CompletableFuture<SendResult<Integer, String>> sendDeviceEvent_sendObject(DeviceEvent deviceEvent)
            throws JsonProcessingException {
//        String value = objectMapper.writeValueAsString(deviceEvent.device());
        String value = objectMapper.writeValueAsString(deviceEvent);

        Integer key = deviceEvent.deviceEventId();
        /*
            producer record is kind of an object which is going to hold the key and value and the topic information.
            can add some additional metadata, such as header equivalent to Http headers.
        * */

        ProducerRecord producerRecord = buildProducerRecord(key, value);
        //1. Blocking first call: to get metadata: => max.block.ms That's the timeout setting for getting the metadata.and then it will throw an error
        CompletableFuture<SendResult<Integer, String>> result
                = kafkaTemplate.send(producerRecord);
        return result.whenComplete((sendResult, throwable) -> {
            if (throwable != null) { //so that means there is some exception that's been experienced.
                handleFailure(key, value, throwable);
            } else {
                handleSuccess(key, value, sendResult); //sendResult holds the information about which partition
                // this particular message is being sent to.
            }
        });
    }

    private ProducerRecord buildProducerRecord(Integer key, String value) {
        List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes(StandardCharsets.UTF_8)));
        //not going to decide which partition this message is going to be. so pass null
        return new ProducerRecord(topic, null, key, value, recordHeaders);
    }


    private void handleSuccess(Integer key, String value, SendResult<Integer, String> sendResult) {
        log.info("Message send successfully for the key {} , value {} , partition is {} ",
                key, value, sendResult.getRecordMetadata().partition());
    }

    private void handleFailure(Integer key, String value, Throwable throwable) {
        log.error("Error sending the message and the exception is {} ", throwable.getMessage(), throwable);
    }
}
