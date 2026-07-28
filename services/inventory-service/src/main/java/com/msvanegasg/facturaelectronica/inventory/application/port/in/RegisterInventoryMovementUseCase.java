package com.msvanegasg.facturaelectronica.inventory.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementQuery;
import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.RegisterInventoryMovementCommand;

public interface RegisterInventoryMovementUseCase {

    InventoryMovementResult register(RegisterInventoryMovementCommand command);

    List<InventoryMovementResult> kardex(UUID companyId, UUID productId);

    List<InventoryMovementResult> kardex(InventoryMovementQuery query);
}
