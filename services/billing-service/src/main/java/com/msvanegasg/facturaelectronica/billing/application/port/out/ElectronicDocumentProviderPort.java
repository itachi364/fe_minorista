package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;

public interface ElectronicDocumentProviderPort {

    ProviderSubmissionResult submit(Sale sale, UUID documentId, ElectronicDocumentType documentType,
            String idempotencyKey);
}