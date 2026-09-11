package com.msvanegasg.facturaelectronica.accounting.interfaces.rest;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.accounting.application.dto.ConfirmPurchaseFiscalCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalDocumentLineCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.PurchaseFiscalConfirmationResult;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ConfirmPurchaseFiscalUseCase;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.PurchaseFiscalConfirmationRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/fiscal-confirmations")
public class PurchaseFiscalConfirmationController {

    private final ConfirmPurchaseFiscalUseCase useCase;

    public PurchaseFiscalConfirmationController(ConfirmPurchaseFiscalUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/purchases")
    public ResponseEntity<PurchaseFiscalConfirmationResult> confirmPurchase(
            @RequestHeader("X-Company-Id") UUID companyId,
            @Valid @RequestBody PurchaseFiscalConfirmationRequest request) {
        return ResponseEntity.ok(useCase.confirm(new ConfirmPurchaseFiscalCommand(companyId, request.sourceId(),
                request.supplierId(), request.operationDate(), request.municipalityCode(), request.creditPurchase(),
                request.dueDate(), request.subtotal(), request.taxTotal(), request.total(), request.contractId(),
                request.lines().stream().map(line -> new FiscalDocumentLineCommand(line.lineId(), line.conceptCode(),
                        line.ciiuCode(), line.taxableBaseAmount(), line.taxAmount(), line.aiuAmount(),
                        line.grossPaymentAmount())).toList())));
    }
}
