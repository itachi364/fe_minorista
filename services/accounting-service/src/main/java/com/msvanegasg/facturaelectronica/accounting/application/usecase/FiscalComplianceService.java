package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalAccountMappingResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountPresentationMappingResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalAuxiliaryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.NationalFiscalConceptResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalPeriodSummary;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalReconciliationResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalReversalResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCertificateResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCertificateIdentity;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageFiscalComplianceUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalComplianceRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.DomainEventPublisherPort;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

@Transactional
public class FiscalComplianceService implements ManageFiscalComplianceUseCase {
    private final FiscalComplianceRepositoryPort repository;
    private final DomainEventPublisherPort eventPublisher;
    private final IdGeneratorPort idGenerator;
    private final Clock clock;

    public FiscalComplianceService(FiscalComplianceRepositoryPort repository) {
        this(repository, DomainEventPublisherPort.noop(), UUID::randomUUID, Clock.systemUTC());
    }

    public FiscalComplianceService(FiscalComplianceRepositoryPort repository, DomainEventPublisherPort eventPublisher,
            IdGeneratorPort idGenerator, Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
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

    @Override
    public WithholdingCertificateResult generateCertificate(UUID companyId, UUID thirdPartyId, int year,
            WithholdingCertificateIdentity identity, UUID userId) {
        if (year < 2000 || year > 2200) throw new IllegalArgumentException("year is invalid");
        Objects.requireNonNull(identity, "certificate identity is required");
        requireText(identity.certificateCity(), "certificateCity");
        requireText(identity.issuerIdentification(), "issuerIdentification");
        requireText(identity.issuerName(), "issuerName");
        requireText(identity.issuerAddress(), "issuerAddress");
        requireText(identity.beneficiaryIdentification(), "beneficiaryIdentification");
        requireText(identity.beneficiaryName(), "beneficiaryName");
        WithholdingCertificateResult result = repository.generateCertificate(Objects.requireNonNull(companyId),
                Objects.requireNonNull(thirdPartyId), year, identity, userId);
        publishCertificateGenerated(result);
        return result;
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

    @Override
    public AccountPresentationMappingResult savePresentationMapping(UUID companyId, UUID accountId,
            String financialReportingGroup, String statementSection, String presentationConcept,
            LocalDate validFrom, LocalDate validTo, String evidenceReference, UUID userId) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(accountId, "accountId is required");
        Objects.requireNonNull(validFrom, "validFrom is required");
        if (!List.of("GRUPO_1", "GRUPO_2", "GRUPO_3").contains(financialReportingGroup)) {
            throw new IllegalArgumentException("financialReportingGroup must be GRUPO_1, GRUPO_2 or GRUPO_3");
        }
        if (statementSection == null || statementSection.isBlank()
                || presentationConcept == null || presentationConcept.isBlank()) {
            throw new IllegalArgumentException("statementSection and presentationConcept are required");
        }
        if (validTo != null && validTo.isBefore(validFrom)) {
            throw new IllegalArgumentException("validTo cannot be before validFrom");
        }
        return repository.savePresentationMapping(companyId, accountId, financialReportingGroup,
                statementSection.trim(), presentationConcept.trim(), validFrom, validTo,
                blankToNull(evidenceReference), userId);
    }

    @Override @Transactional(readOnly = true)
    public List<AccountPresentationMappingResult> findPresentationMappings(UUID companyId) {
        return repository.findPresentationMappings(Objects.requireNonNull(companyId));
    }

    @Override @Transactional(readOnly = true)
    public List<FiscalAuxiliaryResult> form350Auxiliary(UUID companyId, int year, int month) {
        validatePeriod(year, month);
        return repository.findForm350Auxiliary(Objects.requireNonNull(companyId), year, month);
    }

    @Override @Transactional(readOnly = true)
    public List<NationalFiscalConceptResult> nationalConcepts() {
        return repository.findNationalConcepts();
    }

    private static void validatePeriod(int year, int month) {
        if (year < 2000 || year > 2200 || month < 1 || month > 12) {
            throw new IllegalArgumentException("fiscal period is invalid");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
    }

    private void publishCertificateGenerated(WithholdingCertificateResult certificate) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("certificateId", certificate.id().toString());
        payload.put("thirdPartyId", certificate.thirdPartyId().toString());
        payload.put("year", certificate.year());
        payload.put("version", certificate.version());
        payload.put("downloadPath", "/api/v1/withholding-certificates/" + certificate.id() + "/download");
        eventPublisher.publish(new DomainEventEnvelope(idGenerator.newId(),
                EventTypes.WITHHOLDING_CERTIFICATE_GENERATED, 1, clock.instant(), certificate.companyId(),
                "WithholdingCertificate", certificate.id(), "accounting-service", null,
                certificate.id() + ":withholding-certificate-generated", payload));
    }
}
