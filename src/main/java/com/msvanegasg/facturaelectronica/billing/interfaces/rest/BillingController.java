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

import com.msvanegasg.facturaelectronica.billing.application.port.in.ConfigureIssuerProfileUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.CreateNumberingResolutionUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.IssueElectronicPosUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.QueryElectronicPosDocumentUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.SubmitElectronicPosDocumentUseCase;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.ElectronicPosRequest;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.ElectronicPosResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.IssuerProfileRequest;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.IssuerProfileResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.NumberingResolutionRequest;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.NumberingResolutionResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.SubmitElectronicPosResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class BillingController {

    private static final String COMPANY_HEADER = "X-Company-Id";
    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final ConfigureIssuerProfileUseCase configureIssuerProfileUseCase;
    private final CreateNumberingResolutionUseCase createNumberingResolutionUseCase;
    private final IssueElectronicPosUseCase issueElectronicPosUseCase;
    private final QueryElectronicPosDocumentUseCase queryElectronicPosDocumentUseCase;
    private final SubmitElectronicPosDocumentUseCase submitElectronicPosDocumentUseCase;

    public BillingController(
            ConfigureIssuerProfileUseCase configureIssuerProfileUseCase,
            CreateNumberingResolutionUseCase createNumberingResolutionUseCase,
            IssueElectronicPosUseCase issueElectronicPosUseCase,
            QueryElectronicPosDocumentUseCase queryElectronicPosDocumentUseCase,
            SubmitElectronicPosDocumentUseCase submitElectronicPosDocumentUseCase) {
        this.configureIssuerProfileUseCase = configureIssuerProfileUseCase;
        this.createNumberingResolutionUseCase = createNumberingResolutionUseCase;
        this.issueElectronicPosUseCase = issueElectronicPosUseCase;
        this.queryElectronicPosDocumentUseCase = queryElectronicPosDocumentUseCase;
        this.submitElectronicPosDocumentUseCase = submitElectronicPosDocumentUseCase;
    }

    @PostMapping("/issuers")
    public ResponseEntity<IssuerProfileResponse> configureIssuer(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody IssuerProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BillingRestMapper.toResponse(
                        configureIssuerProfileUseCase.configure(BillingRestMapper.toCommand(companyId, request))));
    }

    @PostMapping("/numbering-resolutions")
    public ResponseEntity<NumberingResolutionResponse> createNumberingResolution(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody NumberingResolutionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BillingRestMapper.toResponse(createNumberingResolutionUseCase.create(
                        BillingRestMapper.toCommand(companyId, request))));
    }

    @PostMapping("/electronic-pos")
    public ResponseEntity<ElectronicPosResponse> issueElectronicPos(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody ElectronicPosRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BillingRestMapper.toResponse(
                        issueElectronicPosUseCase.issue(BillingRestMapper.toCommand(companyId, request))));
    }

    @GetMapping("/electronic-pos/{documentId}")
    public ResponseEntity<ElectronicPosResponse> findElectronicPos(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @PathVariable UUID documentId) {
        return ResponseEntity.ok(BillingRestMapper.toResponse(
                queryElectronicPosDocumentUseCase.findById(companyId, documentId)));
    }

    @PostMapping("/electronic-pos/{documentId}/submit")
    public ResponseEntity<SubmitElectronicPosResponse> submitElectronicPos(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestHeader(IDEMPOTENCY_HEADER) String idempotencyKey,
            @PathVariable UUID documentId) {
        return ResponseEntity.ok(BillingRestMapper.toResponse(
                submitElectronicPosDocumentUseCase.submit(companyId, documentId, idempotencyKey)));
    }
}
