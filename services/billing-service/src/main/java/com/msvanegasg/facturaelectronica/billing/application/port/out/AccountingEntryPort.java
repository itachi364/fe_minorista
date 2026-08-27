package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;

public interface AccountingEntryPort {

    default void ensureSalePostingConfigured(UUID companyId) {
        // Default no-op for isolated tests and local adapters that do not require an external accounting check.
    }

    void postSale(Sale sale, String idempotencyKey);
}
