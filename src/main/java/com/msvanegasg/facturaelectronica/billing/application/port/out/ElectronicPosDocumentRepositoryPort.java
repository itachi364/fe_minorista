package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicPosDocument;

public interface ElectronicPosDocumentRepositoryPort {

    ElectronicPosDocument save(ElectronicPosDocument document);

    Optional<ElectronicPosDocument> findByCompanyIdAndDocumentId(UUID companyId, UUID documentId);
}
