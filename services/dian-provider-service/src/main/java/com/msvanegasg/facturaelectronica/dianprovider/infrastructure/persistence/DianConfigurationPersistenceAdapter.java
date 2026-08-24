package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianConfigurationRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.entity.DianCompanyConfigurationJpaEntity;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.repository.DianCompanyConfigurationJpaRepository;

@Component
public class DianConfigurationPersistenceAdapter implements DianConfigurationRepositoryPort {

    private final DianCompanyConfigurationJpaRepository repository;

    public DianConfigurationPersistenceAdapter(DianCompanyConfigurationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public DianCompanyConfiguration save(DianCompanyConfiguration configuration) {
        return toDomain(repository.save(toEntity(configuration)));
    }

    @Override
    public Optional<DianCompanyConfiguration> findByCompanyId(UUID companyId) {
        return repository.findByCompanyId(companyId).map(DianConfigurationPersistenceAdapter::toDomain);
    }

    private static DianCompanyConfigurationJpaEntity toEntity(DianCompanyConfiguration configuration) {
        DianCompanyConfigurationJpaEntity entity = new DianCompanyConfigurationJpaEntity();
        entity.setId(configuration.id());
        entity.setCompanyId(configuration.companyId());
        entity.setMode(configuration.mode());
        entity.setEnvironment(configuration.environment());
        entity.setSoftwareId(configuration.softwareId());
        entity.setSoftwarePinSecretRef(configuration.softwarePinSecretRef());
        entity.setTechnicalKeySecretRef(configuration.technicalKeySecretRef());
        entity.setCertificateSecretRef(configuration.certificateSecretRef());
        entity.setCertificateAlias(configuration.certificateAlias());
        entity.setCertificateFingerprint(configuration.certificateFingerprint());
        entity.setCertificateExpiresAt(configuration.certificateExpiresAt());
        entity.setServiceBaseUrl(configuration.serviceBaseUrl());
        entity.setTestSetId(configuration.testSetId());
        entity.setAcceptedResponsibility(configuration.acceptedResponsibility());
        entity.setStatus(configuration.status());
        entity.setLastTestStatus(configuration.lastTestStatus());
        entity.setLastTestAt(configuration.lastTestAt());
        entity.setLastTestMessage(configuration.lastTestMessage());
        entity.setUpdatedBy(configuration.updatedBy());
        entity.setCreatedAt(configuration.createdAt());
        entity.setUpdatedAt(configuration.updatedAt());
        return entity;
    }

    private static DianCompanyConfiguration toDomain(DianCompanyConfigurationJpaEntity entity) {
        return new DianCompanyConfiguration(entity.getId(), entity.getCompanyId(), entity.getMode(),
                entity.getEnvironment(), entity.getSoftwareId(), entity.getSoftwarePinSecretRef(),
                entity.getTechnicalKeySecretRef(), entity.getCertificateSecretRef(), entity.getCertificateAlias(),
                entity.getCertificateFingerprint(), entity.getCertificateExpiresAt(), entity.getServiceBaseUrl(),
                entity.getTestSetId(), entity.isAcceptedResponsibility(), entity.getStatus(),
                entity.getLastTestStatus(), entity.getLastTestAt(), entity.getLastTestMessage(),
                entity.getUpdatedBy(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
