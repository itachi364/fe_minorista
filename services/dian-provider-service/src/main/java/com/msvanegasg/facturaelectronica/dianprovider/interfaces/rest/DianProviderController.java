package com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.dianprovider.application.port.in.FindProviderSubmissionUseCase;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.in.SubmitProviderDocumentUseCase;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderDocumentType;
import com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest.dto.ProviderSubmissionRequest;
import com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest.dto.ProviderSubmissionResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/provider")
public class DianProviderController {

    private final SubmitProviderDocumentUseCase submitUseCase;
    private final FindProviderSubmissionUseCase findUseCase;

    public DianProviderController(SubmitProviderDocumentUseCase submitUseCase, FindProviderSubmissionUseCase findUseCase) {
        this.submitUseCase = submitUseCase;
        this.findUseCase = findUseCase;
    }

    @PostMapping("/electronic-pos")
    public ProviderSubmissionResponse submitElectronicPos(@RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ProviderSubmissionRequest request) {
        return DianProviderRestMapper
                .toResponse(submitUseCase.submit(DianProviderRestMapper.toCommand(ProviderDocumentType.ELECTRONIC_POS,
                        request, idempotencyKey)));
    }

    @PostMapping("/electronic-invoices")
    public ProviderSubmissionResponse submitElectronicInvoice(@RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ProviderSubmissionRequest request) {
        return DianProviderRestMapper
                .toResponse(submitUseCase.submit(DianProviderRestMapper
                        .toCommand(ProviderDocumentType.ELECTRONIC_INVOICE, request, idempotencyKey)));
    }

    @PostMapping("/credit-notes")
    public ProviderSubmissionResponse submitCreditNote(@RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ProviderSubmissionRequest request) {
        return DianProviderRestMapper
                .toResponse(submitUseCase.submit(DianProviderRestMapper
                        .toCommand(ProviderDocumentType.CREDIT_NOTE, request, idempotencyKey)));
    }

    @PostMapping("/debit-notes")
    public ProviderSubmissionResponse submitDebitNote(@RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ProviderSubmissionRequest request) {
        return DianProviderRestMapper
                .toResponse(submitUseCase.submit(DianProviderRestMapper
                        .toCommand(ProviderDocumentType.DEBIT_NOTE, request, idempotencyKey)));
    }

    @PostMapping("/pos-adjustment-notes")
    public ProviderSubmissionResponse submitPosAdjustmentNote(@RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ProviderSubmissionRequest request) {
        return DianProviderRestMapper
                .toResponse(submitUseCase.submit(DianProviderRestMapper
                        .toCommand(ProviderDocumentType.POS_ADJUSTMENT_NOTE, request, idempotencyKey)));
    }
    @GetMapping("/submissions/{trackingId}")
    public ProviderSubmissionResponse findByTrackingId(@RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable String trackingId) {
        return DianProviderRestMapper.toResponse(findUseCase.findByTrackingId(companyId, trackingId));
    }
}
