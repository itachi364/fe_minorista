package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.billing.application.port.out.IssuerProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.IssuerProfile;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.IssuerProfileJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.IssuerProfileJpaRepository;

@Component
public class IssuerProfilePersistenceAdapter implements IssuerProfileRepositoryPort {

    private final IssuerProfileJpaRepository repository;

    public IssuerProfilePersistenceAdapter(IssuerProfileJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public IssuerProfile save(IssuerProfile issuerProfile) {
        return toDomain(repository.save(toEntity(issuerProfile)));
    }

    @Override
    public Optional<IssuerProfile> findActiveByCompanyId(UUID companyId) {
        return repository.findFirstByCompanyIdAndActiveTrueOrderByIdDesc(companyId)
                .map(IssuerProfilePersistenceAdapter::toDomain);
    }

    private static IssuerProfileJpaEntity toEntity(IssuerProfile issuerProfile) {
        IssuerProfileJpaEntity entity = new IssuerProfileJpaEntity();
        entity.setId(issuerProfile.id());
        entity.setCompanyId(issuerProfile.companyId());
        entity.setLegalName(issuerProfile.legalName());
        entity.setNit(issuerProfile.nit());
        entity.setVerificationDigit(issuerProfile.verificationDigit());
        entity.setTaxResponsibilities(String.join(",", issuerProfile.taxResponsibilities()));
        entity.setMunicipalityCode(issuerProfile.municipalityCode());
        entity.setAddress(issuerProfile.address());
        entity.setActive(issuerProfile.active());
        return entity;
    }

    private static IssuerProfile toDomain(IssuerProfileJpaEntity entity) {
        return IssuerProfile.restore(entity.getId(), entity.getCompanyId(), entity.getLegalName(), entity.getNit(),
                entity.getVerificationDigit(), responsibilities(entity.getTaxResponsibilities()),
                entity.getMunicipalityCode(), entity.getAddress(), entity.isActive());
    }

    private static List<String> responsibilities(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(",")).map(String::trim).filter(item -> !item.isBlank()).toList();
    }
}
