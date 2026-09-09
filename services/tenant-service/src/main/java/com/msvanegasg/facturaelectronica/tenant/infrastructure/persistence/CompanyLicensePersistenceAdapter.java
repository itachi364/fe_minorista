package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyLicenseRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicense;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseModule;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseFeature;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity.CompanyLicenseJpaEntity;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.repository.CompanyLicenseJpaRepository;

@Component
public class CompanyLicensePersistenceAdapter implements CompanyLicenseRepositoryPort {

    private final CompanyLicenseJpaRepository repository;

    public CompanyLicensePersistenceAdapter(CompanyLicenseJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public CompanyLicense save(CompanyLicense license) {
        return toDomain(repository.save(toEntity(license)));
    }

    @Override
    public Optional<CompanyLicense> findByCompanyId(UUID companyId) {
        return repository.findByCompanyId(companyId).map(this::toDomain);
    }

    private CompanyLicenseJpaEntity toEntity(CompanyLicense license) {
        return new CompanyLicenseJpaEntity(
                license.id(),
                license.companyId(),
                license.planCode(),
                license.status(),
                license.validFrom(),
                license.validTo(),
                license.maxUsers(),
                license.maxMonthlyDocuments(),
                toModuleNames(license.enabledModules()),
                toFeatureNames(license.enabledFeatures()),
                license.createdAt(),
                license.updatedAt());
    }

    private CompanyLicense toDomain(CompanyLicenseJpaEntity entity) {
        return new CompanyLicense(
                entity.getId(),
                entity.getCompanyId(),
                entity.getPlanCode(),
                entity.getStatus(),
                entity.getValidFrom(),
                entity.getValidTo(),
                entity.getMaxUsers(),
                entity.getMaxMonthlyDocuments(),
                toModules(entity.getEnabledModules()),
                toFeatures(entity.getEnabledFeatures()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private static String[] toModuleNames(Set<LicenseModule> modules) {
        if (modules == null || modules.isEmpty()) {
            return new String[0];
        }
        return modules.stream()
                .map(LicenseModule::name)
                .sorted()
                .toArray(String[]::new);
    }

    private static String[] toFeatureNames(Set<LicenseFeature> features) {
        if (features == null || features.isEmpty()) {
            return new String[0];
        }
        return features.stream().map(LicenseFeature::name).sorted().toArray(String[]::new);
    }

    private static Set<LicenseModule> toModules(String[] modules) {
        if (modules == null || modules.length == 0) {
            return Set.of();
        }
        return Stream.of(modules)
                .flatMap(value -> {
                    try {
                        return Stream.of(LicenseModule.valueOf(value));
                    } catch (IllegalArgumentException exception) {
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<LicenseFeature> toFeatures(String[] features) {
        if (features == null || features.length == 0) {
            return Set.of();
        }
        return Stream.of(features)
                .flatMap(value -> {
                    try {
                        return Stream.of(LicenseFeature.valueOf(value));
                    } catch (IllegalArgumentException exception) {
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toUnmodifiableSet());
    }
}
