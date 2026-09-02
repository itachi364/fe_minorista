package com.msvanegasg.facturaelectronica.tenant.infrastructure.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyFileStoragePort;

@Component
@ConditionalOnProperty(name = "tenant.files.storage-provider", havingValue = "local", matchIfMissing = true)
public class LocalCompanyFileStorageAdapter implements CompanyFileStoragePort {

    private final Path root;

    public LocalCompanyFileStorageAdapter(
            @Value("${tenant.files.storage-path:${java.io.tmpdir}/nexofiscal/company-files}") String rootPath) {
        this.root = Path.of(rootPath).toAbsolutePath().normalize();
    }

    @Override
    public void save(String storageKey, String contentType, byte[] content) {
        Path target = resolve(storageKey);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible almacenar el archivo empresarial.", exception);
        }
    }

    @Override
    public byte[] read(String storageKey) {
        Path target = resolve(storageKey);
        try {
            return Files.readAllBytes(target);
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible leer el archivo empresarial.", exception);
        }
    }

    private Path resolve(String storageKey) {
        Path target = root.resolve(storageKey).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Invalid company file storage key");
        }
        return target;
    }
}
