package com.msvanegasg.facturaelectronica.tenant.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.application.dto.InvoicingObligationCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.InvoicingObligationResult;

public interface ManageInvoicingObligationUseCase {
    InvoicingObligationResult evaluate(UUID companyId, InvoicingObligationCommand command);
    InvoicingObligationResult current(UUID companyId);
    List<InvoicingObligationResult> history(UUID companyId);
}
