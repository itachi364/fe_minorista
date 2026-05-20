package com.msvanegasg.facturaelectronica.dianprovider.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderDocumentType;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderSubmission;

public interface ProviderSubmissionRepositoryPort {

    ProviderSubmission save(ProviderSubmission submission);

    Optional<ProviderSubmission> findByIdempotencyKey(UUID companyId, UUID documentId,
            ProviderDocumentType documentType, String idempotencyKey);

    Optional<ProviderSubmission> findByTrackingId(String trackingId);

    Optional<ProviderSubmission> findByCompanyIdAndTrackingId(UUID companyId, String trackingId);
}
