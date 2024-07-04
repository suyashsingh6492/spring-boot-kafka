package com.kafka.device.consumer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafka.device.entity.DeviceEvent;
import com.kafka.device.jpa.DeviceEventRepository;
import com.kafka.device.service.DeviceEventService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)  //annotation takes care of spinning up the application context for us.
@EmbeddedKafka(topics = {"device-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}," +
                "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class DeviceEventConsumerIntegrationTest {

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    KafkaListenerEndpointRegistry endpointRegistry; //endpoint registry have a hold of all the listener containers.
    //like deviceEventConsumer

    @SpyBean //Spy Bean is a annotation which will give you the access to the real bean,
    DeviceEventConsumer deviceEventConsumerSpy;

    @SpyBean
    DeviceEventService deviceEventServiceSpy;


    @Autowired
    DeviceEventRepository deviceEventRepository;

    @BeforeEach
    void setUp() {
        // so that the consumer is completely up and running before we go ahead and launch the test
        for (MessageListenerContainer listenerContainer : endpointRegistry.getAllListenerContainers()) {
            //consumer is going to wait until all the partitions are assigned to it so that our test case that we are going to write won't have any issues.
            //Reading the messages from the Kafka topic.
            ContainerTestUtils.waitForAssignment(listenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());

        }
    }

    @AfterEach
    void tearDown() {
        //delete the data after this test case is run, because if
        // you have multiple test cases and if they are sharing in-memory database, there is a possibility you might run into some data issues.
        deviceEventRepository.deleteAll();
    }

    @Test
    void publishNewDeviceEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        String json = "{\"deviceEventId\":null,\"deviceEventType\":\"NEW\",\"device\":{\"deviceSerialNumber\":\"fcd-efg-hij\",\"deviceName\":\"New Device\",\"deviceModelId\":121,\"channel\":\"WEB\"}}";

        kafkaTemplate.sendDefault(json).get(); //sendDefault is async call so make it sync


        //when
        CountDownLatch latch = new CountDownLatch(1); //help us block the current execution of the thread.
        latch.await(3, TimeUnit.SECONDS);
// condition validates whether the Onmessage method in the library events consumer class is invoked once or not.
        verify(deviceEventConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(deviceEventServiceSpy, times(1)).processDeviceEvent(isA(ConsumerRecord.class));

        List<DeviceEvent> deviceEventList = (List<DeviceEvent>) deviceEventRepository.findAll();
        assert deviceEventList.size() == 1;
        deviceEventList.forEach(deviceEvent -> {
            assertEquals("fcd-efg-hij", deviceEvent.getDevice().getDeviceSerialNumber());
        });
    }
}

