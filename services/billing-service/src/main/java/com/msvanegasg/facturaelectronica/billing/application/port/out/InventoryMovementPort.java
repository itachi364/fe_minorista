package com.msvanegasg.facturaelectronica.billing.application.port.out;

import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;

public interface InventoryMovementPort {

    void applySaleOut(Sale sale, String idempotencyKey);
}
