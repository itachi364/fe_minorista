package com.msvanegasg.facturaelectronica.billing.application.port.in;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreatePosAdjustmentNoteCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.PosAdjustmentNoteResult;

public interface CreatePosAdjustmentNoteUseCase {

    PosAdjustmentNoteResult create(CreatePosAdjustmentNoteCommand command);
}
