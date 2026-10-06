package com.demo.payment.service;

import com.demo.payment.dto.TransferResponse;
import org.springframework.http.HttpStatus;

public record TransferOutcome(TransferResponse transfer, HttpStatus httpStatus) {
}
