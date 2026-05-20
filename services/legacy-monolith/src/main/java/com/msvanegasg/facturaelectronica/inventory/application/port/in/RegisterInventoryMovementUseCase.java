package com.msvanegasg.facturaelectronica.inventory.application.port.in;

import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.RegisterInventoryMovementCommand;

public interface RegisterInventoryMovementUseCase {

    InventoryMovementResult register(RegisterInventoryMovementCommand command);
}
