package com.kafka.device.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Device {
    @Id
    String deviceSerialNumber;
    String deviceName;
    Long deviceModelId;
    String channel;

    @OneToOne
    @JoinColumn(name = "deviceEventId") //device table will have device_event_id column
    private DeviceEvent deviceEvent;
}
