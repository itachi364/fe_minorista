package com.msvanegasg.facturaelectronica.inventory.application.port.out;

public interface SupplierLookupPort {

    Long findSupplierIdByDocument(Long documentNumber, Long documentTypeId);
}
