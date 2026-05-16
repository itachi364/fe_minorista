package com.msvanegasg.facturaelectronica.inventory.infrastructure.client;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.client.ProveedorClient;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.SupplierLookupPort;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto.SupplierResponse;

@Component
public class SupplierInventoryAdapter implements SupplierLookupPort {

    private final ProveedorClient proveedorClient;

    public SupplierInventoryAdapter(ProveedorClient proveedorClient) {
        this.proveedorClient = proveedorClient;
    }

    @Override
    public Long findSupplierIdByDocument(Long documentNumber, Long documentTypeId) {
        SupplierResponse supplier = proveedorClient.obtenerProveedorPorDocumentoMigrado(documentNumber, documentTypeId);
        return supplier.getIdProveedor();
    }
}
