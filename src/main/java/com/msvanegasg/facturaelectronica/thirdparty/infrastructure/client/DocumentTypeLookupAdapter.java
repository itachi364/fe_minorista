package com.msvanegasg.facturaelectronica.thirdparty.infrastructure.client;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.DocumentTypeResponse;
import com.msvanegasg.facturaelectronica.client.TipoDocumentoClient;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.DocumentTypeSummary;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.DocumentTypeLookupPort;

@Component
public class DocumentTypeLookupAdapter implements DocumentTypeLookupPort {

    private final TipoDocumentoClient tipoDocumentoClient;

    public DocumentTypeLookupAdapter(TipoDocumentoClient tipoDocumentoClient) {
        this.tipoDocumentoClient = tipoDocumentoClient;
    }

    @Override
    public DocumentTypeSummary findByCode(Long code) {
        DocumentTypeResponse response = tipoDocumentoClient.obtenerTipoDocumentoPorCodigoMigrado(code);
        return new DocumentTypeSummary(response.getCodigo(), response.getNombre());
    }
}
