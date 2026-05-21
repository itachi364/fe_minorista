package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.inventory.application.port.out.InventoryMovementRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovement;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.InventoryMovementJpaEntity;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository.InventoryMovementJpaRepository;

@Component
public class InventoryMovementPersistenceAdapter implements InventoryMovementRepositoryPort {

    private final InventoryMovementJpaRepository repository;

    public InventoryMovementPersistenceAdapter(InventoryMovementJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<InventoryMovement> findIdempotent(UUID companyId, InventorySourceDocumentType sourceType,
            UUID sourceDocumentId, InventoryMovementType movementType, String idempotencyKey) {
        return repository
                .findByCompanyIdAndSourceDocumentTypeAndSourceDocumentIdAndMovementTypeAndIdempotencyKey(companyId,
                        sourceType, sourceDocumentId, movementType, idempotencyKey)
                .map(InventoryMovementPersistenceAdapter::toDomain);
    }

    @Override
    public List<InventoryMovement> findKardex(UUID companyId, UUID productId) {
        return repository.findAllByCompanyIdAndProductIdOrderByMovementAtAsc(companyId, productId).stream()
                .map(InventoryMovementPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public InventoryMovement save(InventoryMovement movement) {
        return toDomain(repository.save(toEntity(movement)));
    }

    private static InventoryMovement toDomain(InventoryMovementJpaEntity entity) {
        return new InventoryMovement(entity.getId(), entity.getCompanyId(), entity.getProductId(),
                entity.getMovementType(), entity.getQuantity(), entity.getUnitCost(), entity.getPreviousStock(),
                entity.getResultingStock(), entity.getSourceDocumentType(), entity.getSourceDocumentId(),
                entity.getIdempotencyKey(), entity.getReason(), entity.getCreatedBy(), entity.getMovementAt());
    }

    private static InventoryMovementJpaEntity toEntity(InventoryMovement movement) {
        InventoryMovementJpaEntity entity = new InventoryMovementJpaEntity();
        entity.setId(movement.id());
        entity.setCompanyId(movement.companyId());
        entity.setProductId(movement.productId());
        entity.setMovementType(movement.movementType());
        entity.setQuantity(movement.quantity());
        entity.setUnitCost(movement.unitCost());
        entity.setPreviousStock(movement.previousStock());
        entity.setResultingStock(movement.resultingStock());
        entity.setSourceDocumentType(movement.sourceDocumentType());
        entity.setSourceDocumentId(movement.sourceDocumentId());
        entity.setIdempotencyKey(movement.idempotencyKey());
        entity.setReason(movement.reason());
        entity.setCreatedBy(movement.createdBy());
        entity.setMovementAt(movement.movementAt());
        return entity;
    }
}
