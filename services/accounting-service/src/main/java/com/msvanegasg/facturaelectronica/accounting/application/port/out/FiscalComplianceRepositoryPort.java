package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalAccountMappingResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalPeriodSummary;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalReconciliationResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalReversalResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCertificateResult;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

public interface FiscalComplianceRepositoryPort {
    FiscalAccountMappingResult saveMapping(UUID companyId, WithholdingType type, String payableAccountCode,
            String receivableAccountCode, LocalDate validFrom, LocalDate validTo, UUID userId);
    List<FiscalAccountMappingResult> findMappings(UUID companyId);
    FiscalReconciliationResult reconcile(UUID companyId, int year, int month);
    FiscalReversalResult reverse(UUID companyId, UUID calculationId, String reason, UUID userId);
    FiscalPeriodSummary summarize(UUID companyId, int year, int month);
    FiscalPeriodSummary close(UUID companyId, int year, int month, UUID userId);
    WithholdingCertificateResult generateCertificate(UUID companyId, UUID thirdPartyId, int year, UUID userId);
    List<WithholdingCertificateResult> findCertificates(UUID companyId, UUID thirdPartyId, int year);
    Optional<String> findCertificateContent(UUID companyId, UUID certificateId);
}
