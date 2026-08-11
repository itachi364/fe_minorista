package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalNoteRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalNote;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.FiscalNoteJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.FiscalNoteJpaRepository;

@Component
public class FiscalNotePersistenceAdapter implements FiscalNoteRepositoryPort {

    private final FiscalNoteJpaRepository repository;

    public FiscalNotePersistenceAdapter(FiscalNoteJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<FiscalNote> findByCompanyIdAndId(UUID companyId, UUID id) {
        return repository.findByCompanyIdAndId(companyId, id).map(FiscalNotePersistenceAdapter::toDomain);
    }

    @Override
    public Optional<FiscalNote> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey) {
        return repository.findByCompanyIdAndIdempotencyKey(companyId, idempotencyKey)
                .map(FiscalNotePersistenceAdapter::toDomain);
    }

    @Override
    public long countIssuedFiscalNotes(UUID companyId, java.time.Instant fromInclusive,
            java.time.Instant toExclusive) {
        return repository.countIssuedFiscalNotes(companyId, fromInclusive, toExclusive);
    }

    @Override
    public FiscalNote save(FiscalNote note) {
        return toDomain(repository.save(toEntity(note)));
    }

    private static FiscalNote toDomain(FiscalNoteJpaEntity entity) {
        return new FiscalNote(entity.getId(), entity.getCompanyId(), entity.getOriginalDocumentId(),
                entity.getNoteType(), entity.getAdjustmentKind(), entity.getStatus(), entity.getProviderStatus(),
                entity.getReason(), entity.getPrefix(), entity.getDocumentNumber(), entity.getCufeCude(),
                entity.getQrContent(), entity.getSubtotal(), entity.getTaxTotal(), entity.getTotal(),
                entity.getProviderTrackingId(), entity.getProviderErrorCode(), entity.getProviderErrorMessage(),
                entity.getIdempotencyKey(), entity.getIssuedAt());
    }

    private static FiscalNoteJpaEntity toEntity(FiscalNote note) {
        FiscalNoteJpaEntity entity = new FiscalNoteJpaEntity();
        entity.setId(note.id());
        entity.setCompanyId(note.companyId());
        entity.setOriginalDocumentId(note.originalDocumentId());
        entity.setNoteType(note.noteType());
        entity.setAdjustmentKind(note.adjustmentKind());
        entity.setStatus(note.status());
        entity.setProviderStatus(note.providerStatus());
        entity.setReason(note.reason());
        entity.setPrefix(note.prefix());
        entity.setDocumentNumber(note.documentNumber());
        entity.setCufeCude(note.cufeCude());
        entity.setQrContent(note.qrContent());
        entity.setSubtotal(note.subtotal());
        entity.setTaxTotal(note.taxTotal());
        entity.setTotal(note.total());
        entity.setProviderTrackingId(note.providerTrackingId());
        entity.setProviderErrorCode(note.providerErrorCode());
        entity.setProviderErrorMessage(note.providerErrorMessage());
        entity.setIdempotencyKey(note.idempotencyKey());
        entity.setIssuedAt(note.issuedAt());
        return entity;
    }
}
