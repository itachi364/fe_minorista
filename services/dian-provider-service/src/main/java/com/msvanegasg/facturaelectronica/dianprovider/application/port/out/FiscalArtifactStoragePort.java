package com.msvanegasg.facturaelectronica.dianprovider.application.port.out;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianArtifactType;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianSubmissionArtifact;

public interface FiscalArtifactStoragePort {

    DianSubmissionArtifact store(UUID companyId, UUID submissionId, UUID documentId, DianArtifactType type,
            String contentType, String fileName, String content, Instant createdAt);
}
