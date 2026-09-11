package com.msvanegasg.facturaelectronica.accounting.interfaces.rest;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CalculateFiscalDocumentCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalDocumentCalculationResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalDocumentLineCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.CalculateFiscalDocumentUseCase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.FiscalDocumentCalculationRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/fiscal-calculations/withholdings")
public class FiscalDocumentCalculationController {
    private final CalculateFiscalDocumentUseCase useCase;

    public FiscalDocumentCalculationController(CalculateFiscalDocumentUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/preview")
    public ResponseEntity<FiscalDocumentCalculationResult> preview(
            @RequestHeader("X-Company-Id") UUID companyId,
            @Valid @RequestBody FiscalDocumentCalculationRequest request) {
        return ResponseEntity.ok(useCase.calculate(toCommand(companyId, request, false)));
    }

    @PostMapping("/documents")
    public ResponseEntity<FiscalDocumentCalculationResult> calculateDocument(
            @RequestHeader("X-Company-Id") UUID companyId,
            @Valid @RequestBody FiscalDocumentCalculationRequest request) {
        return ResponseEntity.ok(useCase.calculate(toCommand(companyId, request, true)));
    }

    @GetMapping("/documents")
    public ResponseEntity<FiscalDocumentCalculationResult> findDocument(
            @RequestHeader("X-Company-Id") UUID companyId,
            @RequestParam AccountingSourceType sourceType,
            @RequestParam UUID sourceId) {
        return useCase.findBySource(companyId, sourceType, sourceId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static CalculateFiscalDocumentCommand toCommand(UUID companyId,
            FiscalDocumentCalculationRequest request, boolean sourceRequired) {
        if (sourceRequired && (request.sourceType() == null || request.sourceId() == null)) {
            throw new IllegalArgumentException("sourceType and sourceId are required for document confirmation");
        }
        return new CalculateFiscalDocumentCommand(companyId, request.operationType(), request.thirdPartyId(),
                request.operationDate(), request.municipalityCode(), sourceRequired ? request.sourceType() : null,
                sourceRequired ? request.sourceId() : null, request.lines().stream()
                        .map(line -> new FiscalDocumentLineCommand(line.lineId(), line.conceptCode(), line.ciiuCode(),
                                line.taxableBaseAmount(), line.taxAmount(), line.aiuAmount(),
                                line.grossPaymentAmount()))
                        .toList(), request.contractId(), request.paymentId());
    }
}
