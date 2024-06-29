package com.kafka.device.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.device.domain.Device;
import com.kafka.device.domain.DeviceEvent;
import com.kafka.util.TestUtil;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"device-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})

//dollar property automatically set by kafka EmbeddedKafka
//make sure package of controller at test and app must same
public class DeviceControllerIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<Integer, String> consumer;


    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        //group-1 unique id of a consumer,Autocommit value as true. This means as soon as the record is read, I'm going to commit the values

        HashMap<String, Object> configConsumer
                = new HashMap<>(KafkaTestUtils.consumerProps("group-1", "true", embeddedKafkaBroker));
        // going to set the auto offset reset value as latest. That means read only the new messages.
        configConsumer.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumer = new DefaultKafkaConsumerFactory<>(configConsumer, new IntegerDeserializer(), new StringDeserializer())
                .createConsumer();
//consumer is going to be read the data from the embedded Kafka broker.
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void postLibraryEvent() {

        //given
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity httpEntity = new HttpEntity(TestUtil.deviceEventRecord(), httpHeaders);
        //when
        //url, mthod, entity obj, return type of ResponseEntity
        //change the call to kafka from controller to syn call to check if embedded kafka working
        ResponseEntity<DeviceEvent> responseEntity
                = restTemplate
                .exchange("/v1/device/event", HttpMethod.POST, httpEntity, DeviceEvent.class);

        //then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        ConsumerRecords<Integer, String> records = KafkaTestUtils.getRecords(consumer);//blocking calls

        assert records.count() == 1; //we are publishing 1 records

        records.forEach(record -> {
                    //when we are sending Device data in kafka topic
                    Device device = TestUtil.parseDeviceEventRecord(objectMapper, record.value());
                    System.out.println("device Test=>" + device);
                    assertEquals(device, TestUtil.deviceRecord());
                }
        );

    }
}
