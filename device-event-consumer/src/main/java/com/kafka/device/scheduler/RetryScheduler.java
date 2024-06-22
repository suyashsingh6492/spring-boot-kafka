package com.kafka.device.scheduler;

import com.kafka.device.config.DeviceEventsConsumerConfig;
import com.kafka.device.entity.FailureRecord;
import com.kafka.device.jpa.FailureRecordRepository;
import com.kafka.device.service.DeviceEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RetryScheduler {

    private FailureRecordRepository failureRecordRepository;
    private DeviceEventService deviceEventService;

    public RetryScheduler(FailureRecordRepository failureRecordRepository, DeviceEventService deviceEventService) {
        this.failureRecordRepository = failureRecordRepository;
        this.deviceEventService = deviceEventService;
    }

    //it's going to be at scheduled, can use this annotation to create a cron job in spring.
    @Scheduled(fixedRate = 10_000) //particular scheduler run every 10s
    public void retryFailedRecords() {
        log.info("Retry Failure records started ");
        failureRecordRepository.findAllByStatus(DeviceEventsConsumerConfig.RETRY)
                .forEach(failureRecord -> {
                    log.info("Retry failed record : {} ",failureRecord);
                    ConsumerRecord<Integer, String> consumerRecord = buildConsumerRecord(failureRecord);
                    try {
                        deviceEventService.processDeviceEvent(consumerRecord);
                        failureRecord.setStatus(DeviceEventsConsumerConfig.SUCCESS);
                        failureRecordRepository.save(failureRecord);
                    } catch (Exception e) {
                        log.error("Exception in retryFailedRecords : {} ", e.getMessage(), e);
                        failureRecord.setErrorRecord(failureRecord.getErrorRecord()
                                .replace("999","null")
                                .replace("UPDATE","NEW")); //hack to
                        // save message so that next time record persisted in  database
                        failureRecordRepository.save(failureRecord);
                    }
                });
        log.info("Retry Failure records ended ");

    }

    private ConsumerRecord<Integer, String> buildConsumerRecord(FailureRecord failureRecord) {
        return new ConsumerRecord<>(
                failureRecord.getTopic(),
                failureRecord.getPartition(),
                failureRecord.getOffset_value(), //given offset need to retry
                failureRecord.getKeyValue(), //key
                failureRecord.getErrorRecord() //value
        );
    }

}
