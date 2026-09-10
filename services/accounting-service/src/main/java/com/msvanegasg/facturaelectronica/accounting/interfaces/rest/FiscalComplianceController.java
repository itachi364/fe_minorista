package com.msvanegasg.facturaelectronica.accounting.interfaces.rest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalAccountMappingResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalPeriodSummary;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalReconciliationResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalReversalResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCertificateResult;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageFiscalComplianceUseCase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/v1")
public class FiscalComplianceController {
    private static final String COMPANY_HEADER = "X-Company-Id";
    private static final String USER_HEADER = "X-User-Id";
    private final ManageFiscalComplianceUseCase useCase;

    public FiscalComplianceController(ManageFiscalComplianceUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/fiscal-account-mappings")
    public List<FiscalAccountMappingResult> mappings(@RequestHeader(COMPANY_HEADER) UUID companyId) {
        return useCase.findMappings(companyId);
    }

    @PutMapping("/fiscal-account-mappings")
    public FiscalAccountMappingResult saveMapping(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestHeader(name = USER_HEADER, required = false) UUID userId,
            @Valid @RequestBody FiscalAccountMappingRequest request) {
        return useCase.saveMapping(companyId, request.withholdingType(), request.payableAccountCode(),
                request.receivableAccountCode(), request.validFrom(), request.validTo(), userId);
    }

    @GetMapping("/fiscal-reconciliation")
    public FiscalReconciliationResult reconcile(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam int year, @RequestParam int month) {
        return useCase.reconcile(companyId, year, month);
    }

    @PostMapping("/fiscal-calculations/withholdings/documents/{calculationId}/reverse")
    public FiscalReversalResult reverse(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestHeader(name = USER_HEADER, required = false) UUID userId, @PathVariable UUID calculationId,
            @Valid @RequestBody FiscalReversalRequest request) {
        return useCase.reverse(companyId, calculationId, request.reason(), userId);
    }

    @GetMapping("/fiscal-periods/{year}/{month}/summary")
    public FiscalPeriodSummary summary(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @PathVariable int year, @PathVariable int month) {
        return useCase.summarize(companyId, year, month);
    }

    @PostMapping("/fiscal-periods/{year}/{month}/close")
    public FiscalPeriodSummary close(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestHeader(name = USER_HEADER, required = false) UUID userId,
            @PathVariable int year, @PathVariable int month) {
        return useCase.close(companyId, year, month, userId);
    }

    @GetMapping("/withholding-certificates")
    public List<WithholdingCertificateResult> certificates(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam UUID thirdPartyId, @RequestParam int year) {
        return useCase.findCertificates(companyId, thirdPartyId, year);
    }

    @PostMapping("/withholding-certificates")
    public WithholdingCertificateResult generateCertificate(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestHeader(name = USER_HEADER, required = false) UUID userId,
            @Valid @RequestBody WithholdingCertificateRequest request) {
        return useCase.generateCertificate(companyId, request.thirdPartyId(), request.year(), userId);
    }

    @GetMapping("/withholding-certificates/{certificateId}/download")
    public ResponseEntity<byte[]> download(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @PathVariable UUID certificateId) {
        byte[] content = useCase.certificateContent(companyId, certificateId).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=certificado-retenciones-" + certificateId + ".csv")
                .body(content);
    }

    public record FiscalAccountMappingRequest(@NotNull WithholdingType withholdingType,
            @NotBlank String payableAccountCode, String receivableAccountCode, @NotNull LocalDate validFrom,
            LocalDate validTo) { }
    public record FiscalReversalRequest(@NotBlank String reason) { }
    public record WithholdingCertificateRequest(@NotNull UUID thirdPartyId, int year) { }
}
