package com.kafka.device.service;

import com.kafka.device.entity.FailureRecord;
import com.kafka.device.jpa.FailureRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FailureService {

    private FailureRecordRepository failureRecordRepository;

    public FailureService(FailureRecordRepository failureRecordRepository) {
        this.failureRecordRepository = failureRecordRepository;
    }

    public void saveFailedRecord(ConsumerRecord<Integer, String> consumerRecord, Exception e, String status) {
        FailureRecord failureRecord = FailureRecord.builder()
                .id(null)
                .topic(consumerRecord.topic())
                .keyValue(consumerRecord.key())
                .errorRecord(consumerRecord.value())
                .partition(consumerRecord.partition())
                .offset_value(consumerRecord.offset())
                .exception(e.getMessage())
                .status(status)
                .build();

        failureRecordRepository.save(failureRecord);
    }
}
