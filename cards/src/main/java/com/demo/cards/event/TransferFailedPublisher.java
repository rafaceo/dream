package com.demo.cards.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class TransferFailedPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final String topic;

    public TransferFailedPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                   JsonMapper jsonMapper,
                                   @Value("${app.kafka.transfer-failed-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.topic = topic;
    }

    public void publish(UUID transferId, String reason) throws Exception {
        String payload = jsonMapper.writeValueAsString(new TransferFailedEvent(transferId, reason, Instant.now()));
        kafkaTemplate.send(topic, transferId.toString(), payload).get(10, TimeUnit.SECONDS);
    }
}
