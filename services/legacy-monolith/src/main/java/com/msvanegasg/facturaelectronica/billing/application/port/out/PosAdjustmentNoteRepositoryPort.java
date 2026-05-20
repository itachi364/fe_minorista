package com.msvanegasg.facturaelectronica.billing.application.port.out;

import com.msvanegasg.facturaelectronica.billing.domain.model.PosAdjustmentNote;

public interface PosAdjustmentNoteRepositoryPort {

    PosAdjustmentNote save(PosAdjustmentNote note);
}
