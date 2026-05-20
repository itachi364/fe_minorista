package com.msvanegasg.facturaelectronica.billing.application.port.out;

import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;

public interface AccountingEntryPort {

    void postSale(Sale sale, String idempotencyKey);
}
