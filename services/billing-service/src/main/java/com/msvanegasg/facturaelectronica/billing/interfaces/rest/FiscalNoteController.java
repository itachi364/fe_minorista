package com.msvanegasg.facturaelectronica.billing.interfaces.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreateFiscalNoteCommand;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ManageFiscalNoteUseCase;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalNoteType;
import com.msvanegasg.facturaelectronica.billing.domain.model.PosAdjustmentKind;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.FiscalNoteRequest;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.FiscalNoteResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class FiscalNoteController {

    private final ManageFiscalNoteUseCase noteUseCase;

    public FiscalNoteController(ManageFiscalNoteUseCase noteUseCase) {
        this.noteUseCase = noteUseCase;
    }

    @PostMapping("/credit-notes")
    public ResponseEntity<FiscalNoteResponse> createCreditNote(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody FiscalNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BillingRestMapper.toResponse(noteUseCase.create(
                toCommand(companyId, FiscalNoteType.CREDIT_NOTE, null, request, idempotencyKey))));
    }

    @GetMapping("/credit-notes/{noteId}")
    public FiscalNoteResponse findCreditNote(@RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable UUID noteId) {
        return BillingRestMapper.toResponse(noteUseCase.findById(companyId, noteId));
    }

    @PostMapping("/debit-notes")
    public ResponseEntity<FiscalNoteResponse> createDebitNote(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody FiscalNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BillingRestMapper.toResponse(noteUseCase.create(
                toCommand(companyId, FiscalNoteType.DEBIT_NOTE, null, request, idempotencyKey))));
    }

    @GetMapping("/debit-notes/{noteId}")
    public FiscalNoteResponse findDebitNote(@RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable UUID noteId) {
        return BillingRestMapper.toResponse(noteUseCase.findById(companyId, noteId));
    }

    @PostMapping("/electronic-pos/{documentId}/adjustment-notes")
    public ResponseEntity<FiscalNoteResponse> createPosAdjustmentNote(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader("Idempotency-Key") String idempotencyKey, @PathVariable UUID documentId,
            @Valid @RequestBody FiscalNoteRequest request) {
        PosAdjustmentKind kind = request.adjustmentKind() == null ? PosAdjustmentKind.CORRECTION
                : request.adjustmentKind();
        FiscalNoteRequest normalized = new FiscalNoteRequest(documentId, kind, request.reason(), request.subtotal(),
                request.taxTotal(), request.total());
        return ResponseEntity.status(HttpStatus.CREATED).body(BillingRestMapper.toResponse(noteUseCase.create(
                toCommand(companyId, FiscalNoteType.POS_ADJUSTMENT_NOTE, kind, normalized, idempotencyKey))));
    }

    private static CreateFiscalNoteCommand toCommand(UUID companyId, FiscalNoteType noteType,
            PosAdjustmentKind adjustmentKind, FiscalNoteRequest request, String idempotencyKey) {
        return new CreateFiscalNoteCommand(companyId, request.originalDocumentId(), noteType, adjustmentKind,
                request.reason(), request.subtotal(), request.taxTotal(), request.total(), idempotencyKey);
    }
}