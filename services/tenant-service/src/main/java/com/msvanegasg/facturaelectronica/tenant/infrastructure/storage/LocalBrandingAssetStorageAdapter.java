package com.msvanegasg.facturaelectronica.tenant.infrastructure.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.tenant.application.port.out.BrandingAssetStoragePort;

@Component
public class LocalBrandingAssetStorageAdapter implements BrandingAssetStoragePort {

    private final Path root;

    public LocalBrandingAssetStorageAdapter(
            @Value("${tenant.branding.storage-path:${java.io.tmpdir}/nexofiscal/tenant-branding}") String rootPath) {
        this.root = Path.of(rootPath).toAbsolutePath().normalize();
    }

    @Override
    public void save(String storageKey, byte[] content) {
        Path target = resolve(storageKey);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible almacenar el asset de branding.", exception);
        }
    }

    @Override
    public byte[] read(String storageKey) {
        Path target = resolve(storageKey);
        try {
            return Files.readAllBytes(target);
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible leer el asset de branding.", exception);
        }
    }

    private Path resolve(String storageKey) {
        Path target = root.resolve(storageKey).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Invalid branding storage key");
        }
        return target;
    }
}
