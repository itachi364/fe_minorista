package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.InventoryMovementJpaEntity;

public interface InventoryMovementJpaRepository extends JpaRepository<InventoryMovementJpaEntity, UUID> {

    Optional<InventoryMovementJpaEntity> findByCompanyIdAndSourceDocumentTypeAndSourceDocumentIdAndMovementTypeAndIdempotencyKey(
            UUID companyId, InventorySourceDocumentType sourceDocumentType, UUID sourceDocumentId,
            InventoryMovementType movementType, String idempotencyKey);

    List<InventoryMovementJpaEntity> findAllByCompanyIdAndProductIdOrderByMovementAtAsc(UUID companyId, UUID productId);
}
