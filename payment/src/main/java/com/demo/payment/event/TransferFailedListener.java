package com.demo.payment.event;

import com.demo.payment.service.TransferStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferFailedListener {

    private final TransferStatusService transferStatusService;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = "${app.kafka.transfer-failed-topic}")
    public void onTransferFailed(String payload) {
        TransferFailedEvent event;
        try {
            event = jsonMapper.readValue(payload, TransferFailedEvent.class);
        } catch (Exception e) {
            log.error("Skipping unreadable transfer.failed message: {}", payload, e);
            return;
        }
        transferStatusService.markFailed(event.transferId(), event.reason());
    }
}
