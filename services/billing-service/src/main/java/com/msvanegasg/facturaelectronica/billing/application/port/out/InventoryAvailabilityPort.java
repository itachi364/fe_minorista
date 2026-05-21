package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.InventoryProductSnapshot;

public interface InventoryAvailabilityPort {

    InventoryProductSnapshot findProduct(UUID companyId, UUID productId);

    boolean isAvailable(UUID companyId, UUID productId, BigDecimal quantity);
}
