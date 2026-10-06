package com.demo.cards.event;

import com.demo.cards.service.TransferProcessingResult;
import com.demo.cards.service.TransferProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferCompletedListener {

    private final TransferProcessingService transferProcessingService;
    private final TransferFailedPublisher transferFailedPublisher;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = "${app.kafka.transfer-completed-topic}")
    public void onTransferCompleted(String payload) throws Exception {
        TransferCompletedEvent event;
        try {
            event = jsonMapper.readValue(payload, TransferCompletedEvent.class);
        } catch (Exception e) {
            log.error("Skipping unreadable transfer.completed message: {}", payload, e);
            return;
        }

        TransferProcessingResult result = transferProcessingService.apply(event);

        if (result.isRejected()) {
            transferFailedPublisher.publish(event.transferId(), result.reason());
        }
    }
}
