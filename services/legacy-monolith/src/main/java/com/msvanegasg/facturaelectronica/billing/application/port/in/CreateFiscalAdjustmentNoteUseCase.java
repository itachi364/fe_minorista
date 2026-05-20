package com.msvanegasg.facturaelectronica.billing.application.port.in;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreateFiscalAdjustmentNoteCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalAdjustmentNoteResult;

public interface CreateFiscalAdjustmentNoteUseCase {

    FiscalAdjustmentNoteResult create(CreateFiscalAdjustmentNoteCommand command);
}
