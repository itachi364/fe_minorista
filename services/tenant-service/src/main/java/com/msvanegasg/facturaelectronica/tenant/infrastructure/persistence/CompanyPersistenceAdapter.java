package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.Company;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity.CompanyJpaEntity;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.repository.CompanyJpaRepository;

@Component
public class CompanyPersistenceAdapter implements CompanyRepositoryPort {

    private final CompanyJpaRepository repository;

    public CompanyPersistenceAdapter(CompanyJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Company save(Company company) {
        return toDomain(repository.save(toEntity(company)));
    }

    @Override
    public Optional<Company> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsByIdentification(UUID identificationTypeId, String identificationNumber) {
        return repository.existsByIdentificationTypeIdAndIdentificationNumber(identificationTypeId,
                identificationNumber);
    }

    private CompanyJpaEntity toEntity(Company company) {
        return new CompanyJpaEntity(
                company.id(),
                company.legalName(),
                company.tradeName(),
                company.identificationTypeId(),
                company.identificationNumber(),
                company.verificationDigit(),
                company.email(),
                company.status(),
                company.createdAt(),
                company.updatedAt());
    }

    private Company toDomain(CompanyJpaEntity entity) {
        return new Company(
                entity.getId(),
                entity.getLegalName(),
                entity.getTradeName(),
                entity.getIdentificationTypeId(),
                entity.getIdentificationNumber(),
                entity.getVerificationDigit(),
                entity.getEmail(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
