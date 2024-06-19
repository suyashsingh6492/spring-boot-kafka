package com.kafka.device.domain;

public record DeviceEvent(
        Long deviceEventId,
        DeviceEventType deviceEventType,
        Device device

) {

}
