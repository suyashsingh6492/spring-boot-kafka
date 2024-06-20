package com.kafka.device.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafka.device.domain.DeviceEvent;
import com.kafka.device.producer.DeviceEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@Slf4j
public class DeviceEventsController {

    private final DeviceEventProducer deviceEventProducer;

    public DeviceEventsController(DeviceEventProducer deviceEventProducer) {
        this.deviceEventProducer = deviceEventProducer;
    }

    @PostMapping("/v1/device/event")
    public ResponseEntity<DeviceEvent> postDeviceEvent(
            @RequestBody DeviceEvent deviceEvent
    ) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        log.info("deviceEvent {}", deviceEvent);
        //invoke the kafka producer
        // deviceEventProducer.sendDeviceEvent(deviceEvent);
       // deviceEventProducer.sendDeviceEvent_Sync(deviceEvent);
        deviceEventProducer.sendDeviceEvent_sendObject(deviceEvent);
        log.info("After sending deviceEvent.");
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceEvent);
    }

}
