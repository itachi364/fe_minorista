package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.billing.application.port.out.NumberingResolutionRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;
import com.msvanegasg.facturaelectronica.billing.domain.model.NumberingResolution;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.BillingNumberingResolutionJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.BillingNumberingResolutionJpaRepository;

@Component
public class BillingNumberingResolutionPersistenceAdapter implements NumberingResolutionRepositoryPort {

    private final BillingNumberingResolutionJpaRepository repository;

    public BillingNumberingResolutionPersistenceAdapter(BillingNumberingResolutionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public NumberingResolution save(NumberingResolution numberingResolution) {
        return toDomain(repository.save(toEntity(numberingResolution)));
    }

    @Override
    public Optional<NumberingResolution> findActiveResolution(UUID companyId, ElectronicDocumentType documentType,
            FiscalEnvironment environment, LocalDate documentDate) {
        return repository
                .findFirstByCompanyIdAndDocumentTypeAndEnvironmentAndActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByValidToAsc(
                        companyId, documentType, environment, documentDate, documentDate)
                .map(BillingNumberingResolutionPersistenceAdapter::toDomain);
    }

    private static BillingNumberingResolutionJpaEntity toEntity(NumberingResolution resolution) {
        return BillingNumberingResolutionJpaEntity.builder()
                .id(resolution.id())
                .companyId(resolution.companyId())
                .documentType(resolution.documentType())
                .resolutionNumber(resolution.resolutionNumber())
                .prefix(resolution.prefix())
                .fromNumber(resolution.fromNumber())
                .toNumber(resolution.toNumber())
                .currentNumber(resolution.currentNumber())
                .validFrom(resolution.validFrom())
                .validTo(resolution.validTo())
                .environment(resolution.environment())
                .active(resolution.active())
                .build();
    }

    private static NumberingResolution toDomain(BillingNumberingResolutionJpaEntity entity) {
        return NumberingResolution.restore(
                entity.getId(),
                entity.getCompanyId(),
                entity.getDocumentType(),
                entity.getResolutionNumber(),
                entity.getPrefix(),
                entity.getFromNumber(),
                entity.getToNumber(),
                entity.getCurrentNumber(),
                entity.getValidFrom(),
                entity.getValidTo(),
                entity.getEnvironment(),
                Boolean.TRUE.equals(entity.getActive()));
    }
}
