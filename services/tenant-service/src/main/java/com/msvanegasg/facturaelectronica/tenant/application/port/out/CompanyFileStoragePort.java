package com.msvanegasg.facturaelectronica.tenant.application.port.out;

import java.time.Duration;
import java.util.Optional;

public interface CompanyFileStoragePort {

    void save(String storageKey, String contentType, byte[] content);

    byte[] read(String storageKey);

    default Optional<String> temporaryReadUrl(String storageKey, Duration ttl) {
        return Optional.empty();
    }
}
