package com.msvanegasg.facturaelectronica.inventory.application.port.out;

import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovement;

public interface InventoryMovementRepositoryPort {

    InventoryMovement save(InventoryMovement movement);
}
