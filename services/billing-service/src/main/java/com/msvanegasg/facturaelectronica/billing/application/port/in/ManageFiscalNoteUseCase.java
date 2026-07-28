package com.msvanegasg.facturaelectronica.billing.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreateFiscalNoteCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNoteResult;

public interface ManageFiscalNoteUseCase {

    FiscalNoteResult create(CreateFiscalNoteCommand command);

    FiscalNoteResult findById(UUID companyId, UUID noteId);
}