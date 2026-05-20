package com.msvanegasg.facturaelectronica.billing.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.SubmitElectronicPosDocumentResult;

public interface SubmitElectronicPosDocumentUseCase {

    SubmitElectronicPosDocumentResult submit(UUID companyId, UUID documentId, String idempotencyKey);
}
