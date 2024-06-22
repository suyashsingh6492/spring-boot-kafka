package com.kafka.device.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.device.entity.DeviceEvent;
import com.kafka.device.jpa.DeviceEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class DeviceEventService {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private DeviceEventRepository deviceEventRepository;

    public void processDeviceEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {

        DeviceEvent deviceEvent = objectMapper.readValue(consumerRecord.value(), DeviceEvent.class);
        log.info("deviceEvent : {} ",deviceEvent);


        switch (deviceEvent.getDeviceEventType()){
            case NEW -> {
                
                save(deviceEvent);
            }
            case UPDATE -> {
                validate(deviceEvent);
                save(deviceEvent);
            }
            default -> {
                log.info("Invalid Device event type!");
            }
        }


    }

    private void validate(DeviceEvent deviceEvent){
        if(deviceEvent.getDeviceEventId()==999){
            //simulate for network failure
            throw new RecoverableDataAccessException("Temporary Network Issue!");
        }
        if(deviceEvent.getDeviceEventId()==null){
            throw new IllegalArgumentException("Device Event Id is missing !");
        }

        Optional<DeviceEvent> deviceEventOptional = deviceEventRepository.findById(deviceEvent.getDeviceEventId());
        if(deviceEventOptional.isEmpty()){
            throw new IllegalArgumentException("Not a valid Device Event Id!");

        }

        log.info("Validation succesful for the device event {} ",deviceEventOptional.get());
    }
    private void save(DeviceEvent deviceEvent) {
        deviceEvent.getDevice().setDeviceEvent(deviceEvent); //So if you don't do this, then there is not going to be any value in the mapping column.
        deviceEventRepository.save(deviceEvent);

        log.info("Successfully Persisted the library event {} ",deviceEvent);
    }
}
