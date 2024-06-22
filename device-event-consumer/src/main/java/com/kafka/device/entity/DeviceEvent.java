package com.kafka.device.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DeviceEvent {
    @Id
    @GeneratedValue  //part of jakarta package
    private Integer deviceEventId;
    @Enumerated(EnumType.STRING)
    private DeviceEventType deviceEventType;
//So anytime I delete a device event, I want the device event also to be deleted automatically anytime.
    @OneToOne(mappedBy = "deviceEvent", cascade = {CascadeType.ALL})
    @ToString.Exclude //But if you are logging it, since we have the reference of book and library event back to back in these entities, if you don't do this, you will end up having a out of memory error.
    private Device device;
}
