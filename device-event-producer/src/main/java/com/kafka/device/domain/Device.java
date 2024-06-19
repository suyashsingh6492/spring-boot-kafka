package com.kafka.device.domain;

import java.util.UUID;

public record Device (
        String deviceSerialNumber,
        String deviceName,
        Long deviceModelId,
        String channel
){
}
