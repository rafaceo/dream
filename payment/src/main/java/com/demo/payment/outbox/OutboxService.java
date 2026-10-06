package com.demo.payment.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(String topic, String key, Object event) {
        outboxEventRepository.save(OutboxEvent.builder()
                .topic(topic)
                .eventKey(key)
                .payload(jsonMapper.writeValueAsString(event))
                .build());
    }
}
