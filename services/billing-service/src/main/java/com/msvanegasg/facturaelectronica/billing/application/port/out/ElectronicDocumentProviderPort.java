package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;

public interface ElectronicDocumentProviderPort {

    ProviderSubmissionResult submitElectronicPos(Sale sale, UUID documentId, String idempotencyKey);
}
