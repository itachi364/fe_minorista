package com.msvanegasg.facturaelectronica.billing.application.port.out;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentDraft;

public interface ElectronicDocumentDraftRepositoryPort {

    ElectronicDocumentDraft save(ElectronicDocumentDraft draft);
}
