package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalAccountMappingResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalPeriodSummary;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalReconciliationResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalReversalResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCertificateResult;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageFiscalComplianceUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalComplianceRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

@Transactional
public class FiscalComplianceService implements ManageFiscalComplianceUseCase {
    private final FiscalComplianceRepositoryPort repository;

    public FiscalComplianceService(FiscalComplianceRepositoryPort repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public FiscalAccountMappingResult saveMapping(UUID companyId, WithholdingType type, String payableAccountCode,
            String receivableAccountCode, LocalDate validFrom, LocalDate validTo, UUID userId) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(type, "withholdingType is required");
        Objects.requireNonNull(validFrom, "validFrom is required");
        if (payableAccountCode == null || !payableAccountCode.matches("\\d+")) {
            throw new IllegalArgumentException("payableAccountCode must identify a numeric company account");
        }
        if (validTo != null && validTo.isBefore(validFrom)) {
            throw new IllegalArgumentException("validTo cannot be before validFrom");
        }
        return repository.saveMapping(companyId, type, payableAccountCode, blankToNull(receivableAccountCode),
                validFrom, validTo, userId);
    }

    @Override @Transactional(readOnly = true)
    public List<FiscalAccountMappingResult> findMappings(UUID companyId) {
        return repository.findMappings(Objects.requireNonNull(companyId));
    }

    @Override @Transactional(readOnly = true)
    public FiscalReconciliationResult reconcile(UUID companyId, int year, int month) {
        validatePeriod(year, month);
        return repository.reconcile(Objects.requireNonNull(companyId), year, month);
    }

    @Override
    public FiscalReversalResult reverse(UUID companyId, UUID calculationId, String reason, UUID userId) {
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("reason is required");
        return repository.reverse(Objects.requireNonNull(companyId), Objects.requireNonNull(calculationId),
                reason.trim(), userId);
    }

    @Override @Transactional(readOnly = true)
    public FiscalPeriodSummary summarize(UUID companyId, int year, int month) {
        validatePeriod(year, month);
        return repository.summarize(Objects.requireNonNull(companyId), year, month);
    }

    @Override
    public FiscalPeriodSummary close(UUID companyId, int year, int month, UUID userId) {
        validatePeriod(year, month);
        return repository.close(Objects.requireNonNull(companyId), year, month, userId);
    }

    @Override
    public WithholdingCertificateResult generateCertificate(UUID companyId, UUID thirdPartyId, int year,
            UUID userId) {
        if (year < 2000 || year > 2200) throw new IllegalArgumentException("year is invalid");
        return repository.generateCertificate(Objects.requireNonNull(companyId),
                Objects.requireNonNull(thirdPartyId), year, userId);
    }

    @Override @Transactional(readOnly = true)
    public List<WithholdingCertificateResult> findCertificates(UUID companyId, UUID thirdPartyId, int year) {
        if (year < 2000 || year > 2200) throw new IllegalArgumentException("year is invalid");
        return repository.findCertificates(Objects.requireNonNull(companyId), Objects.requireNonNull(thirdPartyId),
                year);
    }

    @Override @Transactional(readOnly = true)
    public String certificateContent(UUID companyId, UUID certificateId) {
        return repository.findCertificateContent(Objects.requireNonNull(companyId),
                Objects.requireNonNull(certificateId))
                .orElseThrow(() -> new IllegalArgumentException("withholding certificate was not found"));
    }

    private static void validatePeriod(int year, int month) {
        if (year < 2000 || year > 2200 || month < 1 || month > 12) {
            throw new IllegalArgumentException("fiscal period is invalid");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
