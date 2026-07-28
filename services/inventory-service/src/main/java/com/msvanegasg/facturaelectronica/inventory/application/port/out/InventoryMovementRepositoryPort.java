package com.msvanegasg.facturaelectronica.inventory.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovement;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;

public interface InventoryMovementRepositoryPort {

    Optional<InventoryMovement> findIdempotent(UUID companyId, InventorySourceDocumentType sourceType,
            UUID sourceDocumentId, InventoryMovementType movementType, String idempotencyKey);

    List<InventoryMovement> findKardex(UUID companyId, UUID productId);

    List<InventoryMovement> findKardex(UUID companyId, UUID productId, LocalDate from, LocalDate to);

    InventoryMovement save(InventoryMovement movement);
}
