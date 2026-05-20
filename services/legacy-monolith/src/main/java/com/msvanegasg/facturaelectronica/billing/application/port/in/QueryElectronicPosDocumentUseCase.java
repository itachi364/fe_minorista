package com.msvanegasg.facturaelectronica.billing.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicPosDocumentResult;

public interface QueryElectronicPosDocumentUseCase {

    ElectronicPosDocumentResult findById(UUID companyId, UUID documentId);
}
