package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.filesystem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.FiscalArtifactStoragePort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.CudeHashGenerator;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianArtifactType;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianSubmissionArtifact;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.config.DianProviderProperties;

@Component
public class LocalFiscalArtifactStorageAdapter implements FiscalArtifactStoragePort {

    private final Path root;
    private final IdGeneratorPort idGenerator;

    public LocalFiscalArtifactStorageAdapter(DianProviderProperties properties, IdGeneratorPort idGenerator) {
        this.root = Path.of(properties.artifactStorageRoot()).normalize();
        this.idGenerator = idGenerator;
    }

    @Override
    public DianSubmissionArtifact store(UUID companyId, UUID submissionId, UUID documentId, DianArtifactType type,
            String contentType, String fileName, String content, Instant createdAt) {
        String safeFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path target = root.resolve(companyId.toString()).resolve(submissionId.toString()).resolve(safeFileName)
                .normalize();
        try {
            Files.createDirectories(target.getParent());
            byte[] bytes = content == null ? new byte[0] : content.getBytes(StandardCharsets.UTF_8);
            Files.write(target, bytes);
            return new DianSubmissionArtifact(idGenerator.generate(), companyId, submissionId, documentId, type,
                    "local-private-dian-artifacts", target.toString(), contentType, safeFileName,
                    "sha256:" + CudeHashGenerator.generate(content == null ? "" : content), (long) bytes.length,
                    createdAt, null);
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible almacenar artefacto fiscal DIAN.", exception);
        }
    }
}
