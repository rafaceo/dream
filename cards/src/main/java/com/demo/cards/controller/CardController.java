package com.demo.cards.controller;

import com.demo.cards.dto.CardRequest;
import com.demo.cards.dto.CardResponse;
import com.demo.cards.dto.CardUpdateRequest;
import com.demo.cards.exception.ApiError;
import com.demo.cards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Cards", description = "Bank cards management")
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @Operation(summary = "Add a card", description = "The card network is detected from the card number.")
    @ApiResponse(responseCode = "201", description = "Card created")
    @ApiResponse(responseCode = "400", description = "Validation failed or unsupported card network",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Card with this number already exists",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    public ResponseEntity<CardResponse> addCard(@Valid @RequestBody CardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.addCard(request));
    }

    @Operation(summary = "Get a card by id")
    @ApiResponse(responseCode = "200", description = "Card found")
    @ApiResponse(responseCode = "404", description = "Card not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{cardId}")
    public ResponseEntity<CardResponse> getCard(@Parameter(description = "Card id") @PathVariable UUID cardId) {
        return ResponseEntity.ok(cardService.getCard(cardId));
    }

    @Operation(summary = "Get all cards of an owner")
    @ApiResponse(responseCode = "200", description = "Cards of the owner, possibly empty")
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<CardResponse>> getCardsByOwner(
            @Parameter(description = "Owner id") @PathVariable UUID ownerId) {
        return ResponseEntity.ok(cardService.getCardsByOwner(ownerId));
    }

    @Operation(summary = "Change card status", description = "Status is the only field that can be changed on an existing card.")
    @ApiResponse(responseCode = "200", description = "Card updated")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Card not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PutMapping("/{cardId}")
    public ResponseEntity<CardResponse> updateCard(@Parameter(description = "Card id") @PathVariable UUID cardId,
                                                   @Valid @RequestBody CardUpdateRequest request) {
        return ResponseEntity.ok(cardService.updateCard(cardId, request));
    }

    @Operation(summary = "Delete a card")
    @ApiResponse(responseCode = "200", description = "Card deleted")
    @ApiResponse(responseCode = "404", description = "Card not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@Parameter(description = "Card id") @PathVariable UUID cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok().build();
    }
}
