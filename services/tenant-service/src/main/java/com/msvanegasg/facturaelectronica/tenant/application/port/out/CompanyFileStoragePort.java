package com.msvanegasg.facturaelectronica.tenant.application.port.out;

public interface CompanyFileStoragePort {

    void save(String storageKey, String contentType, byte[] content);

    byte[] read(String storageKey);
}
