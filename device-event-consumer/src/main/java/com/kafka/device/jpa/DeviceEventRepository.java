package com.kafka.device.jpa;

import com.kafka.device.entity.DeviceEvent;
import org.springframework.data.repository.CrudRepository;

public interface DeviceEventRepository extends CrudRepository<DeviceEvent, Integer> {
}
