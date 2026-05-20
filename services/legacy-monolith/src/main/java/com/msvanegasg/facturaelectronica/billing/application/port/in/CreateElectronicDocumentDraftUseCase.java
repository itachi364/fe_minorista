package com.msvanegasg.facturaelectronica.billing.application.port.in;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreateElectronicDocumentDraftCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentDraftResult;

public interface CreateElectronicDocumentDraftUseCase {

    ElectronicDocumentDraftResult createDraft(CreateElectronicDocumentDraftCommand command);
}
