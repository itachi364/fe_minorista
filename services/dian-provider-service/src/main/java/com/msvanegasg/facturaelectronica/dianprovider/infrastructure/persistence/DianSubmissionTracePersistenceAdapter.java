package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence;

import java.util.Collection;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianSubmissionTraceRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianSubmissionArtifact;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianSubmissionEvent;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianTechnicalValidationResult;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.entity.DianSubmissionArtifactJpaEntity;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.entity.DianSubmissionEventJpaEntity;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.entity.DianTechnicalValidationResultJpaEntity;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.repository.DianSubmissionArtifactJpaRepository;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.repository.DianSubmissionEventJpaRepository;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.repository.DianTechnicalValidationResultJpaRepository;

@Component
public class DianSubmissionTracePersistenceAdapter implements DianSubmissionTraceRepositoryPort {

    private final DianSubmissionEventJpaRepository eventRepository;
    private final DianTechnicalValidationResultJpaRepository validationRepository;
    private final DianSubmissionArtifactJpaRepository artifactRepository;

    public DianSubmissionTracePersistenceAdapter(DianSubmissionEventJpaRepository eventRepository,
            DianTechnicalValidationResultJpaRepository validationRepository,
            DianSubmissionArtifactJpaRepository artifactRepository) {
        this.eventRepository = eventRepository;
        this.validationRepository = validationRepository;
        this.artifactRepository = artifactRepository;
    }

    @Override
    public void saveEvent(DianSubmissionEvent event) {
        DianSubmissionEventJpaEntity entity = new DianSubmissionEventJpaEntity();
        entity.setId(event.id());
        entity.setCompanyId(event.companyId());
        entity.setSubmissionId(event.submissionId());
        entity.setDocumentId(event.documentId());
        entity.setEventType(event.eventType());
        entity.setStatus(event.status());
        entity.setDianCode(event.dianCode());
        entity.setDianMessage(event.dianMessage());
        entity.setCorrelationId(event.correlationId());
        entity.setCreatedAt(event.createdAt());
        eventRepository.save(entity);
    }

    @Override
    public void saveValidationResults(Collection<DianTechnicalValidationResult> results) {
        validationRepository.saveAll(results.stream().map(this::toEntity).toList());
    }

    @Override
    public void saveArtifact(DianSubmissionArtifact artifact) {
        DianSubmissionArtifactJpaEntity entity = new DianSubmissionArtifactJpaEntity();
        entity.setId(artifact.id());
        entity.setCompanyId(artifact.companyId());
        entity.setSubmissionId(artifact.submissionId());
        entity.setDocumentId(artifact.documentId());
        entity.setArtifactType(artifact.artifactType());
        entity.setStorageBucketReference(artifact.storageBucketReference());
        entity.setStorageKey(artifact.storageKey());
        entity.setContentType(artifact.contentType());
        entity.setFileName(artifact.fileName());
        entity.setContentHash(artifact.contentHash());
        entity.setSizeBytes(artifact.sizeBytes());
        entity.setCreatedAt(artifact.createdAt());
        entity.setCreatedBy(artifact.createdBy());
        artifactRepository.save(entity);
    }

    private DianTechnicalValidationResultJpaEntity toEntity(DianTechnicalValidationResult result) {
        DianTechnicalValidationResultJpaEntity entity = new DianTechnicalValidationResultJpaEntity();
        entity.setId(result.id());
        entity.setCompanyId(result.companyId());
        entity.setSubmissionId(result.submissionId());
        entity.setDocumentId(result.documentId());
        entity.setValidationType(result.validationType());
        entity.setResult(result.result());
        entity.setRuleCode(result.ruleCode());
        entity.setMessage(result.message());
        entity.setSourceVersion(result.sourceVersion());
        entity.setValidatedAt(result.validatedAt());
        return entity;
    }
}
