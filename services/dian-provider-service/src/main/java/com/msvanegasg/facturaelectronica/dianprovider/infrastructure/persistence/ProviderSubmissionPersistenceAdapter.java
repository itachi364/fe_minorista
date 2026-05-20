package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.ProviderSubmissionRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderDocumentType;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderSubmission;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.entity.ProviderSubmissionJpaEntity;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.repository.ProviderSubmissionJpaRepository;

@Component
public class ProviderSubmissionPersistenceAdapter implements ProviderSubmissionRepositoryPort {

    private final ProviderSubmissionJpaRepository repository;

    public ProviderSubmissionPersistenceAdapter(ProviderSubmissionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProviderSubmission save(ProviderSubmission submission) {
        return toDomain(repository.save(toEntity(submission)));
    }

    @Override
    public Optional<ProviderSubmission> findByIdempotencyKey(UUID companyId, UUID documentId,
            ProviderDocumentType documentType, String idempotencyKey) {
        return repository
                .findByCompanyIdAndDocumentIdAndDocumentTypeAndIdempotencyKey(companyId, documentId, documentType,
                        idempotencyKey)
                .map(ProviderSubmissionPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<ProviderSubmission> findByTrackingId(String trackingId) {
        return repository.findByTrackingId(trackingId).map(ProviderSubmissionPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<ProviderSubmission> findByCompanyIdAndTrackingId(UUID companyId, String trackingId) {
        return repository.findByCompanyIdAndTrackingId(companyId, trackingId)
                .map(ProviderSubmissionPersistenceAdapter::toDomain);
    }

    private static ProviderSubmissionJpaEntity toEntity(ProviderSubmission submission) {
        ProviderSubmissionJpaEntity entity = new ProviderSubmissionJpaEntity();
        entity.setId(submission.id());
        entity.setCompanyId(submission.companyId());
        entity.setDocumentId(submission.documentId());
        entity.setDocumentType(submission.documentType());
        entity.setIdempotencyKey(submission.idempotencyKey());
        entity.setTrackingId(submission.trackingId());
        entity.setStatus(submission.status());
        entity.setCufeCude(submission.cufeCude());
        entity.setQrContent(submission.qrContent());
        entity.setErrorCode(submission.errorCode());
        entity.setErrorMessage(submission.errorMessage());
        entity.setCreatedAt(submission.createdAt());
        entity.setRawRequest(submission.rawRequest());
        entity.setRawResponse(submission.rawResponse());
        return entity;
    }

    private static ProviderSubmission toDomain(ProviderSubmissionJpaEntity entity) {
        return new ProviderSubmission(entity.getId(), entity.getCompanyId(), entity.getDocumentId(),
                entity.getDocumentType(), entity.getIdempotencyKey(), entity.getTrackingId(), entity.getStatus(),
                entity.getCufeCude(), entity.getQrContent(), entity.getErrorCode(), entity.getErrorMessage(),
                entity.getCreatedAt(), entity.getRawRequest(), entity.getRawResponse());
    }
}
