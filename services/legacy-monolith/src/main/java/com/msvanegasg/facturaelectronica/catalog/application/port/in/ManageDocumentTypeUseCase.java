package com.msvanegasg.facturaelectronica.catalog.application.port.in;

import java.util.List;

import com.msvanegasg.facturaelectronica.catalog.application.dto.DocumentTypeCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.DocumentType;

public interface ManageDocumentTypeUseCase {

    List<DocumentType> findAll();

    List<DocumentType> findActive();

    List<DocumentType> findInactive();

    DocumentType findByCode(Long code);

    DocumentType create(DocumentTypeCommand command);

    DocumentType update(Long code, DocumentTypeCommand command);

    void disable(Long code);

    void enable(Long code);
}
