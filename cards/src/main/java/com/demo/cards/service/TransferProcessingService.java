package com.demo.cards.service;

import com.demo.cards.event.TransferCompletedEvent;

public interface TransferProcessingService {

    TransferProcessingResult apply(TransferCompletedEvent event);
}
