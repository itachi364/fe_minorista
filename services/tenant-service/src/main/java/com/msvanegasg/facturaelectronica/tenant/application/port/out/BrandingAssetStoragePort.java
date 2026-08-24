package com.msvanegasg.facturaelectronica.tenant.application.port.out;

public interface BrandingAssetStoragePort {

    void save(String storageKey, byte[] content);

    byte[] read(String storageKey);
}
