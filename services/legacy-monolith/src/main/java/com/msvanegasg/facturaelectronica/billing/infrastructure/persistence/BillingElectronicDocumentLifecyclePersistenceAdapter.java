package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentLifecycleRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentLifecycle;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.BillingElectronicPosDocumentJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.BillingElectronicPosDocumentJpaRepository;

@Component
public class BillingElectronicDocumentLifecyclePersistenceAdapter implements ElectronicDocumentLifecycleRepositoryPort {

    private final BillingElectronicPosDocumentJpaRepository documentRepository;

    public BillingElectronicDocumentLifecyclePersistenceAdapter(
            BillingElectronicPosDocumentJpaRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public Optional<ElectronicDocumentLifecycle> findByCompanyIdAndDocumentId(UUID companyId, UUID documentId) {
        return documentRepository.findByCompanyIdAndId(companyId, documentId)
                .map(BillingElectronicDocumentLifecyclePersistenceAdapter::toLifecycle);
    }

    @Override
    public ElectronicDocumentLifecycle save(ElectronicDocumentLifecycle document) {
        BillingElectronicPosDocumentJpaEntity entity = documentRepository.findByCompanyIdAndId(
                document.companyId(),
                document.id())
                .orElseThrow(() -> new IllegalStateException("electronic POS document was not found"));
        entity.setStatus(document.status());
        entity.setProviderSubmissionId(document.providerSubmissionId());
        entity.setProviderCufeCude(document.cufeCude());
        entity.setProviderQrContent(document.qrContent());
        entity.setProviderXmlContent(document.xmlContent());
        entity.setProviderGraphicRepresentationContent(document.graphicRepresentationContent());
        entity.setProviderErrorCode(document.errorCode());
        entity.setProviderErrorMessage(document.errorMessage());
        entity.setUpdatedAt(document.updatedAt());
        return toLifecycle(documentRepository.save(entity));
    }

    private static ElectronicDocumentLifecycle toLifecycle(BillingElectronicPosDocumentJpaEntity document) {
        Instant updatedAt = document.getUpdatedAt() == null ? document.getIssueAt() : document.getUpdatedAt();
        return ElectronicDocumentLifecycle.restore(
                document.getId(),
                document.getCompanyId(),
                ElectronicDocumentType.ELECTRONIC_POS,
                document.getStatus(),
                document.getProviderSubmissionId(),
                document.getProviderCufeCude(),
                document.getProviderQrContent(),
                document.getProviderXmlContent(),
                document.getProviderGraphicRepresentationContent(),
                document.getProviderErrorCode(),
                document.getProviderErrorMessage(),
                updatedAt);
    }
}
