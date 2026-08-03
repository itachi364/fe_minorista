package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import java.util.List;
import java.util.Objects;

import com.msvanegasg.facturaelectronica.catalog.application.dto.DocumentTypeCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageDocumentTypeUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.DocumentTypeRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.DocumentType;
import com.msvanegasg.facturaelectronica.exception.tipodocumento.TipoDocumentoNotFoundException;

public class DocumentTypeManagementService implements ManageDocumentTypeUseCase {

    private final DocumentTypeRepositoryPort documentTypeRepository;

    public DocumentTypeManagementService(DocumentTypeRepositoryPort documentTypeRepository) {
        this.documentTypeRepository = Objects.requireNonNull(documentTypeRepository);
    }

    @Override
    public List<DocumentType> findAll() {
        return documentTypeRepository.findAll();
    }

    @Override
    public List<DocumentType> findActive() {
        return documentTypeRepository.findActive();
    }

    @Override
    public List<DocumentType> findInactive() {
        return documentTypeRepository.findInactive();
    }

    @Override
    public DocumentType findByCode(Integer code) {
        return documentTypeRepository.findByCode(code)
                .orElseThrow(() -> new TipoDocumentoNotFoundException(code));
    }

    @Override
    public DocumentType create(DocumentTypeCommand command) {
        Objects.requireNonNull(command, "command is required");
        return documentTypeRepository.save(DocumentType.create(command.code(), command.name(), command.description()));
    }

    @Override
    public DocumentType update(Integer code, DocumentTypeCommand command) {
        Objects.requireNonNull(command, "command is required");
        DocumentType existing = findByCode(code);
        return documentTypeRepository.save(existing.update(command.name(), command.description()));
    }

    @Override
    public void disable(Integer code) {
        DocumentType existing = findByCode(code);
        documentTypeRepository.save(existing.disable());
    }

    @Override
    public void enable(Integer code) {
        DocumentType existing = findByCode(code);
        if (!existing.active()) {
            documentTypeRepository.save(existing.enable());
        }
    }
}
