package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.catalog.application.port.out.DocumentTypeRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.DocumentType;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.DocumentTypeJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.DocumentTypeJpaRepository;

@Component
public class DocumentTypePersistenceAdapter implements DocumentTypeRepositoryPort {

    private final DocumentTypeJpaRepository documentTypeRepository;

    public DocumentTypePersistenceAdapter(DocumentTypeJpaRepository documentTypeRepository) {
        this.documentTypeRepository = documentTypeRepository;
    }

    @Override
    public List<DocumentType> findAll() {
        return documentTypeRepository.findAll().stream()
                .map(DocumentTypePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<DocumentType> findActive() {
        return documentTypeRepository.findByActivoTrue().stream()
                .map(DocumentTypePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<DocumentType> findInactive() {
        return documentTypeRepository.findByActivoFalse().stream()
                .map(DocumentTypePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<DocumentType> findByCode(Long code) {
        return documentTypeRepository.findById(code)
                .map(DocumentTypePersistenceAdapter::toDomain);
    }

    @Override
    public DocumentType save(DocumentType documentType) {
        DocumentTypeJpaEntity saved = documentTypeRepository.save(toEntity(documentType));
        return toDomain(saved);
    }

    private static DocumentType toDomain(DocumentTypeJpaEntity entity) {
        return DocumentType.restore(
                entity.getCodigo(),
                entity.getNombre(),
                entity.getDescripcion(),
                Boolean.TRUE.equals(entity.getActivo()));
    }

    private static DocumentTypeJpaEntity toEntity(DocumentType documentType) {
        return DocumentTypeJpaEntity.builder()
                .codigo(documentType.code())
                .nombre(documentType.name())
                .descripcion(documentType.description())
                .activo(documentType.active())
                .build();
    }
}
