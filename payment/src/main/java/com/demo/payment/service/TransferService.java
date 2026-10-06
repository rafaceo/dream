package com.demo.payment.service;

import com.demo.payment.dto.PageResponse;
import com.demo.payment.dto.TransferRequest;
import com.demo.payment.dto.TransferResponse;
import com.demo.payment.entity.TransferStatus;

import java.util.UUID;

public interface TransferService {

    TransferOutcome transfer(TransferRequest request);

    TransferResponse getTransfer(UUID transferId);

    PageResponse<TransferResponse> getTransfers(UUID cardId, TransferStatus status, int page, int size);
}
