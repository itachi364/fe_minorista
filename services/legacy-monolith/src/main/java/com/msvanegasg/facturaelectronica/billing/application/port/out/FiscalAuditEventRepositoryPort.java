package com.msvanegasg.facturaelectronica.billing.application.port.out;

import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalAuditEvent;

public interface FiscalAuditEventRepositoryPort {

    FiscalAuditEvent save(FiscalAuditEvent event);
}
