package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.billing.application.port.out.ProviderSubmissionRecordRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionRecord;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.BillingProviderSubmissionJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.BillingProviderSubmissionJpaRepository;

@Component
public class BillingProviderSubmissionPersistenceAdapter implements ProviderSubmissionRecordRepositoryPort {

    private final BillingProviderSubmissionJpaRepository repository;

    public BillingProviderSubmissionPersistenceAdapter(BillingProviderSubmissionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProviderSubmissionRecord save(ProviderSubmissionRecord submissionRecord) {
        return toDomain(repository.save(toEntity(submissionRecord)));
    }

    private static BillingProviderSubmissionJpaEntity toEntity(ProviderSubmissionRecord record) {
        return BillingProviderSubmissionJpaEntity.builder()
                .id(record.id())
                .companyId(record.companyId())
                .documentId(record.documentId())
                .documentType(record.documentType())
                .idempotencyKey(record.idempotencyKey())
                .requestPayloadHash(record.requestPayloadHash())
                .status(record.status())
                .providerSubmissionId(record.providerSubmissionId())
                .cufeCude(record.cufeCude())
                .qrContent(record.qrContent())
                .xmlContent(record.xmlContent())
                .graphicRepresentationContent(record.graphicRepresentationContent())
                .errorCode(record.errorCode())
                .errorMessage(record.errorMessage())
                .submittedAt(record.submittedAt())
                .build();
    }

    private static ProviderSubmissionRecord toDomain(BillingProviderSubmissionJpaEntity entity) {
        return new ProviderSubmissionRecord(
                entity.getId(),
                entity.getCompanyId(),
                entity.getDocumentId(),
                entity.getDocumentType(),
                entity.getIdempotencyKey(),
                entity.getRequestPayloadHash(),
                entity.getStatus(),
                entity.getProviderSubmissionId(),
                entity.getCufeCude(),
                entity.getQrContent(),
                entity.getXmlContent(),
                entity.getGraphicRepresentationContent(),
                entity.getErrorCode(),
                entity.getErrorMessage(),
                entity.getSubmittedAt());
    }
}
