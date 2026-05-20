package com.msvanegasg.facturaelectronica.billing.application.port.out;

import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalAdjustmentNote;

public interface FiscalAdjustmentNoteRepositoryPort {

    FiscalAdjustmentNote save(FiscalAdjustmentNote note);
}
