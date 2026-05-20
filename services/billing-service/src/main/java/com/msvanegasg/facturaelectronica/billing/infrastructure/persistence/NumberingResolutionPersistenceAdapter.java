package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.billing.application.port.out.NumberingResolutionRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;
import com.msvanegasg.facturaelectronica.billing.domain.model.NumberingResolution;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.NumberingResolutionJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.NumberingResolutionJpaRepository;

@Component
public class NumberingResolutionPersistenceAdapter implements NumberingResolutionRepositoryPort {

    private final NumberingResolutionJpaRepository repository;

    public NumberingResolutionPersistenceAdapter(NumberingResolutionJpaRepository repository) {
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
                .map(NumberingResolutionPersistenceAdapter::toDomain);
    }

    @Override
    public List<NumberingResolution> findByCompanyId(UUID companyId, ElectronicDocumentType documentType,
            Boolean active) {
        List<NumberingResolutionJpaEntity> results;
        if (documentType != null && active != null) {
            results = repository.findByCompanyIdAndDocumentTypeAndActiveOrderByValidToDesc(companyId, documentType,
                    active);
        } else if (documentType != null) {
            results = repository.findByCompanyIdAndDocumentTypeOrderByValidToDesc(companyId, documentType);
        } else if (active != null) {
            results = repository.findByCompanyIdAndActiveOrderByValidToDesc(companyId, active);
        } else {
            results = repository.findByCompanyIdOrderByValidToDesc(companyId);
        }
        return results.stream().map(NumberingResolutionPersistenceAdapter::toDomain).toList();
    }

    private static NumberingResolutionJpaEntity toEntity(NumberingResolution resolution) {
        NumberingResolutionJpaEntity entity = new NumberingResolutionJpaEntity();
        entity.setId(resolution.id());
        entity.setCompanyId(resolution.companyId());
        entity.setDocumentType(resolution.documentType());
        entity.setResolutionNumber(resolution.resolutionNumber());
        entity.setPrefix(resolution.prefix());
        entity.setFromNumber(resolution.fromNumber());
        entity.setToNumber(resolution.toNumber());
        entity.setCurrentNumber(resolution.currentNumber());
        entity.setValidFrom(resolution.validFrom());
        entity.setValidTo(resolution.validTo());
        entity.setEnvironment(resolution.environment());
        entity.setActive(resolution.active());
        return entity;
    }

    private static NumberingResolution toDomain(NumberingResolutionJpaEntity entity) {
        return NumberingResolution.restore(entity.getId(), entity.getCompanyId(), entity.getDocumentType(),
                entity.getResolutionNumber(), entity.getPrefix(), entity.getFromNumber(), entity.getToNumber(),
                entity.getCurrentNumber(), entity.getValidFrom(), entity.getValidTo(), entity.getEnvironment(),
                entity.isActive());
    }
}
