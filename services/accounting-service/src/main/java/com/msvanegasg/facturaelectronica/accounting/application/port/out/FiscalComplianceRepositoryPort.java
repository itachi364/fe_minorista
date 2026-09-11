package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalAccountMappingResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountPresentationMappingResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalAuxiliaryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.NationalFiscalConceptResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalPeriodSummary;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalReconciliationResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalReversalResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCertificateResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCertificateIdentity;
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
    default WithholdingCertificateResult generateCertificate(UUID companyId, UUID thirdPartyId, int year,
            WithholdingCertificateIdentity identity, UUID userId) {
        return generateCertificate(companyId, thirdPartyId, year, userId);
    }
    List<WithholdingCertificateResult> findCertificates(UUID companyId, UUID thirdPartyId, int year);
    Optional<String> findCertificateContent(UUID companyId, UUID certificateId);
    default AccountPresentationMappingResult savePresentationMapping(UUID companyId, UUID accountId,
            String financialReportingGroup, String statementSection, String presentationConcept,
            LocalDate validFrom, LocalDate validTo, String evidenceReference, UUID userId) {
        throw new UnsupportedOperationException("presentation mappings are not available");
    }
    default List<AccountPresentationMappingResult> findPresentationMappings(UUID companyId) { return List.of(); }
    default List<FiscalAuxiliaryResult> findForm350Auxiliary(UUID companyId, int year, int month) { return List.of(); }
    default List<NationalFiscalConceptResult> findNationalConcepts() { return List.of(); }
}
