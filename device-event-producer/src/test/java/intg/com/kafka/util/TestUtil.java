package com.kafka.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.device.domain.Device;
import com.kafka.device.domain.DeviceEvent;
import com.kafka.device.domain.DeviceEventType;

public class TestUtil {

    public static Device deviceRecord() {

        return new Device("fcd-efg-hij",
                "New Device", 121l, "WEB");
    }

    public static Device deviceRecordInvalidValue() {
        return new Device(null,
                "", 121l, "WEB");
    }

    public static DeviceEvent deviceEventRecord() {

        return new DeviceEvent(null, DeviceEventType.NEW, deviceRecord());
    }

    public static DeviceEvent deviceEventRecordWithId() {

        return new DeviceEvent(123, DeviceEventType.NEW, deviceRecord());
    }

    public static DeviceEvent deviceEventRecordUpdate() {

        return new DeviceEvent(123, DeviceEventType.UPDATE, deviceRecord());
    }

    public static DeviceEvent deviceEventRecordInvalidDevice() {

        return new DeviceEvent(null, DeviceEventType.NEW, deviceRecordInvalidValue());
    }


    public static Device parseDeviceEventRecord(ObjectMapper objectMapper , String json){

        try {
            return  objectMapper.readValue(json, Device.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }



}
