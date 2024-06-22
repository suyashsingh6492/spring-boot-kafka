package com.kafka.device.config;

import com.kafka.device.service.FailureService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.ContainerCustomizer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Objects;

@Slf4j
@Configuration
//@EnableKafka //the consumer that we are going to build is going to be automatically spun up when you start up the application.
public class DeviceEventsConsumerConfig {

    public static final String RETRY = "RETRY";
    public static final String DEAD = "DEAD";
    public static final String SUCCESS = "SUCCESS";
    @Autowired
    KafkaTemplate kafkaTemplate; //producer configuration stored in yml file

    @Value("${topics.retry}")
    private String retryTopic;


    @Value("${topics.dead-letter}")
    private String dtlTopic;

    @Autowired
    private FailureService failureService;

    // search KafkaAnnotationDrivenConfiguration .class , copy below method


    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
                                                                                ObjectProvider<ConsumerFactory<Object, Object>> kafkaConsumerFactory,
                                                                                ObjectProvider<ContainerCustomizer<Object, Object, ConcurrentMessageListenerContainer<Object, Object>>> kafkaContainerCustomizer,
                                                                                ObjectProvider<SslBundles> sslBundles) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory();
        configurer.configure(factory, kafkaConsumerFactory.getIfAvailable());
        Objects.requireNonNull(factory);
        factory.setCommonErrorHandler(errorHandler()); //common error get from consumer ,So the interval is always one second between each and every retry attempt.

        //set ack mode manual
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        //set concurrency is a method which we can use in order to configure multiple Kafka listeners from the same application itself.
        factory.setConcurrency(3); //we have 3 partitions it is going to spawn three threads with the same instance of the Kafka listener.
        //see container is running on different thread ntainer#0-0-C-1, not main thread when application start up
        //with above line we have 3 threads
        kafkaContainerCustomizer.ifAvailable(factory::setContainerCustomizer);
        return factory;
    }


    public DefaultErrorHandler errorHandler() {
        // what I would like to implement here is that I would like to have a backoff of one second between
        // each and every retry attempt And then I would like to be retrying just twice.

        //like to retry each attempt with a second delay in between.
        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 2); //1 actual event , 2 retry, so total 3

        //this time interval gradually increase per retry attempt.
        ExponentialBackOffWithMaxRetries expBackOff = new ExponentialBackOffWithMaxRetries(2);// exponential backoff code to attempt to retry only twice.
        expBackOff.setInitialInterval(1_000L);// first value is going to be having a delay of one second.
        expBackOff.setMultiplier(2.0); //So the next attempt will be two times from the time this value is.so after 3 sec
        expBackOff.setMaxInterval(2_000l); //max interval to go until two seconds multiplier comes into play and it's going to be waiting until
        //      the two second and then it's going to be completing the retry behavior.,Set the maximum back off time in milliseconds.


        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(//go inside this method, check desc,
                consumerRecordRecoverer, //save record into db, functional interface impl
                //publishingRecoverer(),    // in case of the retry or exhausted., this will publish in topic
                //fixedBackOff
                expBackOff
        );

        defaultErrorHandler.addRetryableExceptions(RecoverableDataAccessException.class); //for network failure, we provide the exceptions to the config that we would like to attempt to retry.
        //we will be continuously retrying it, but there is not going to be  any useful outcome out of it.
        // So in those kind of scenarios, we would like to ignore certain exceptions and retry only the specific exceptions.
        //defaultErrorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        // defaultErrorHandler.addNotRetryableExceptions();//Those exceptions won't be retried whenever an exception is thrown from the Kafka consumer.

        // The point of having retry listener is to make sure and monitor what happens in each and every retry attempt.
        defaultErrorHandler.setRetryListeners(((record, ex, deliveryAttempt) -> {
            log.info("Failed Records in Retry listener , Exception {} , deliveryAttempt {} ", ex.getMessage(), deliveryAttempt);
        })); //retrylistner is functional interface,
        return defaultErrorHandler;
    }


    public DeadLetterPublishingRecoverer publishingRecoverer() {
        //So basically it's going to publish the message into another Kafka topic. So basically it's going to publish the message into another Kafka topic.
        // In that case it requires a Kafka template.
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer( //So this is a class which is responsible for adding all the necessary headers for you out of the box
                //DLT_EXCEPTION_STACKTRACE, DLT_EXCEPTION_MESSAGE, DLT_ORIGINAL_OFFSET etc
                kafkaTemplate,
                (r, e) -> {
                    log.error("Exception in publishingRecoverer : {} ",e.getMessage(),e);

                    if (e.getCause() instanceof RecoverableDataAccessException) {
                        return new TopicPartition(retryTopic, r.partition());
                    } else {
                        return new TopicPartition(dtlTopic, r.partition());
                    }
                }
        );

        return recoverer;
    }


    //save error in db , DeadLetterPublishingRecoverer also implement ConsumerRecordRecoverer
    ConsumerRecordRecoverer consumerRecordRecoverer= (consumerRecord, e) -> {
        log.error("Exception in consumerRecordRecoverer : {} ",e.getMessage(),e);
        ConsumerRecord<Integer, String> record = (ConsumerRecord<Integer, String>) consumerRecord;
        if (e.getCause() instanceof RecoverableDataAccessException) {
            log.info("Inside Recovery ");
           //recovery logic
            failureService.saveFailedRecord(record,e, RETRY);
        } else {
            log.info("Inside Non-Recovery ");

            //non-recovery logic
            failureService.saveFailedRecord(record,e, DEAD);
        }
    };
}
