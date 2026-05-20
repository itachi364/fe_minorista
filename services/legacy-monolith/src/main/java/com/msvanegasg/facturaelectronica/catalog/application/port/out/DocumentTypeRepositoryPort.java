package com.msvanegasg.facturaelectronica.catalog.application.port.out;

import java.util.List;
import java.util.Optional;

import com.msvanegasg.facturaelectronica.catalog.domain.model.DocumentType;

public interface DocumentTypeRepositoryPort {

    List<DocumentType> findAll();

    List<DocumentType> findActive();

    List<DocumentType> findInactive();

    Optional<DocumentType> findByCode(Long code);

    DocumentType save(DocumentType documentType);
}
