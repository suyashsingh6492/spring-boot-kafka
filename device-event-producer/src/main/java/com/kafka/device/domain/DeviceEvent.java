package com.kafka.device.domain;

public record DeviceEvent(
        Integer deviceEventId,
        DeviceEventType deviceEventType,
        Device device

) {

}
