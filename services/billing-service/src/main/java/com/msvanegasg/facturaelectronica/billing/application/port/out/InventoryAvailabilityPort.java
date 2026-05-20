package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface InventoryAvailabilityPort {

    boolean isAvailable(UUID companyId, UUID productId, BigDecimal quantity);
}
