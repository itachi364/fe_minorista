package com.msvanegasg.facturaelectronica.catalog.interfaces.rest;

import com.msvanegasg.facturaelectronica.catalog.application.dto.DocumentTypeCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.DocumentType;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.DocumentTypeRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.DocumentTypeResponse;

public final class DocumentTypeRestMapper {

    private DocumentTypeRestMapper() {
    }

    public static DocumentTypeCommand toCommand(DocumentTypeRequest dto) {
        return new DocumentTypeCommand(dto.getCodigo(), dto.getNombre(), dto.getDescripcion());
    }

    public static DocumentTypeResponse toResponse(DocumentType documentType) {
        return DocumentTypeResponse.builder()
                .codigo(documentType.code())
                .nombre(documentType.name())
                .descripcion(documentType.description())
                .activo(documentType.active())
                .build();
    }

    public static DocumentTypeRequest toRequest(DocumentType documentType) {
        return DocumentTypeRequest.builder()
                .codigo(documentType.code())
                .nombre(documentType.name())
                .descripcion(documentType.description())
                .build();
    }
}
