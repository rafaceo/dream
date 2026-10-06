package com.demo.payment.controller;

import com.demo.payment.dto.PageResponse;
import com.demo.payment.dto.TransferRequest;
import com.demo.payment.dto.TransferResponse;
import com.demo.payment.entity.TransferStatus;
import com.demo.payment.exception.ApiError;
import com.demo.payment.service.TransferOutcome;
import com.demo.payment.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Transfers", description = "Money transfers between cards")
@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @Operation(summary = "Create a transfer",
            description = "Validates both cards in the cards service, saves the transfer and publishes transfer.completed "
                    + "through the outbox. The status may later change to FAILED if cards rejects the transfer.")
    @ApiResponse(responseCode = "200", description = "Transfer accepted")
    @ApiResponse(responseCode = "400", description = "Invalid request body",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Source or destination card not found",
            content = @Content(schema = @Schema(implementation = TransferResponse.class)))
    @ApiResponse(responseCode = "422", description = "Card is not ACTIVE, currencies differ or insufficient funds",
            content = @Content(schema = @Schema(implementation = TransferResponse.class)))
    @ApiResponse(responseCode = "503", description = "Cards service is unavailable",
            content = @Content(schema = @Schema(implementation = TransferResponse.class)))
    @PostMapping
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransferOutcome outcome = transferService.transfer(request);
        return ResponseEntity.status(outcome.httpStatus()).body(outcome.transfer());
    }

    @Operation(summary = "Get a transfer by id")
    @ApiResponse(responseCode = "200", description = "Transfer found")
    @ApiResponse(responseCode = "400", description = "Malformed transfer id",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Transfer not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{transferId}")
    public ResponseEntity<TransferResponse> getTransfer(
            @Parameter(description = "Transfer id") @PathVariable UUID transferId) {
        return ResponseEntity.ok(transferService.getTransfer(transferId));
    }

    @Operation(summary = "Transfer history",
            description = "Newest first. Filters are optional; cardId matches transfers where the card is the source or the destination.")
    @ApiResponse(responseCode = "200", description = "Page of transfers")
    @ApiResponse(responseCode = "400", description = "Invalid filter or paging parameters",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping
    public ResponseEntity<PageResponse<TransferResponse>> getTransfers(
            @Parameter(description = "Only transfers where this card is the source or the destination")
            @RequestParam(required = false) UUID cardId,
            @Parameter(description = "Only transfers with this status")
            @RequestParam(required = false) TransferStatus status,
            @Parameter(description = "Zero-based page number")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page must not be negative") int page,
            @Parameter(description = "Page size, 1 to 100")
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size must be at least 1")
            @Max(value = 100, message = "size must not exceed 100") int size) {
        return ResponseEntity.ok(transferService.getTransfers(cardId, status, page, size));
    }
}
