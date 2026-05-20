package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderDocumentType;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.entity.ProviderSubmissionJpaEntity;

public interface ProviderSubmissionJpaRepository extends JpaRepository<ProviderSubmissionJpaEntity, UUID> {

    Optional<ProviderSubmissionJpaEntity> findByCompanyIdAndDocumentIdAndDocumentTypeAndIdempotencyKey(UUID companyId,
            UUID documentId, ProviderDocumentType documentType, String idempotencyKey);

    Optional<ProviderSubmissionJpaEntity> findByTrackingId(String trackingId);

    Optional<ProviderSubmissionJpaEntity> findByCompanyIdAndTrackingId(UUID companyId, String trackingId);
}
