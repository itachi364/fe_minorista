package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.billing.application.port.out.IssuerProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.IssuerProfile;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.BillingIssuerProfileJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.BillingIssuerProfileJpaRepository;

@Component
public class BillingIssuerProfilePersistenceAdapter implements IssuerProfileRepositoryPort {

    private final BillingIssuerProfileJpaRepository repository;

    public BillingIssuerProfilePersistenceAdapter(BillingIssuerProfileJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public IssuerProfile save(IssuerProfile issuerProfile) {
        return toDomain(repository.save(toEntity(issuerProfile)));
    }

    @Override
    public Optional<IssuerProfile> findActiveByCompanyId(java.util.UUID companyId) {
        return repository.findFirstByCompanyIdAndActiveTrueOrderByIdDesc(companyId)
                .map(BillingIssuerProfilePersistenceAdapter::toDomain);
    }

    private static BillingIssuerProfileJpaEntity toEntity(IssuerProfile issuerProfile) {
        return BillingIssuerProfileJpaEntity.builder()
                .id(issuerProfile.id())
                .companyId(issuerProfile.companyId())
                .legalName(issuerProfile.legalName())
                .nit(issuerProfile.nit())
                .verificationDigit(issuerProfile.verificationDigit())
                .taxResponsibilities(String.join(",", issuerProfile.taxResponsibilities()))
                .municipalityCode(issuerProfile.municipalityCode())
                .address(issuerProfile.address())
                .active(issuerProfile.active())
                .build();
    }

    private static IssuerProfile toDomain(BillingIssuerProfileJpaEntity entity) {
        return IssuerProfile.restore(
                entity.getId(),
                entity.getCompanyId(),
                entity.getLegalName(),
                entity.getNit(),
                entity.getVerificationDigit(),
                responsibilities(entity.getTaxResponsibilities()),
                entity.getMunicipalityCode(),
                entity.getAddress(),
                Boolean.TRUE.equals(entity.getActive()));
    }

    private static List<String> responsibilities(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }
}
