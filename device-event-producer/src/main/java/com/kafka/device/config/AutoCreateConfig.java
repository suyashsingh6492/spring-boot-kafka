package com.kafka.device.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class AutoCreateConfig {

    @Value("${spring.kafka.topic}")
    public String topic;

    @Value("${topics.retry}")
    private String retryTopic;

    @Value("${topics.dead-letter}")
    private String dtlTopic;

    @Bean
    public NewTopic deviceEventNewTopic() {
        return TopicBuilder.name(topic)
                .partitions(3)
                .replicas(3) //we have three broker Kafka cluster setup
                .build();
    }

    @Bean
    public NewTopic deviceEventRetryOnFailureTopic() {
        return TopicBuilder.name(retryTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic deviceEventDeadLetterTopic() {
        return TopicBuilder.name(dtlTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

}
