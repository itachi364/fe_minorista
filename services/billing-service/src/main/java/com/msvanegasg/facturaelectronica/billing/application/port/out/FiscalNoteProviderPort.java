package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.Map;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

public interface FiscalNoteProviderPort {

    ProviderSubmissionResult submit(UUID companyId, UUID noteId, ElectronicDocumentType documentType,
            Map<String, Object> payload, String idempotencyKey);
}