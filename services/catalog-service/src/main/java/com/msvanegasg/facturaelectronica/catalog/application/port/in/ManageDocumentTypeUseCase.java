package com.msvanegasg.facturaelectronica.catalog.application.port.in;

import java.util.List;

import com.msvanegasg.facturaelectronica.catalog.application.dto.DocumentTypeCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.DocumentType;

public interface ManageDocumentTypeUseCase {

    List<DocumentType> findAll();

    List<DocumentType> findActive();

    List<DocumentType> findInactive();

    DocumentType findByCode(Integer code);

    DocumentType create(DocumentTypeCommand command);

    DocumentType update(Integer code, DocumentTypeCommand command);

    void disable(Integer code);

    void enable(Integer code);
}
