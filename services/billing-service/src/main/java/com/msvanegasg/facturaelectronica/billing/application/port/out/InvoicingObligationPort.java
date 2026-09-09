package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.UUID;

public interface InvoicingObligationPort {
    boolean allowsNonFiscalSale(UUID companyId);

    static InvoicingObligationPort allowAll() {
        return companyId -> true;
    }
}
