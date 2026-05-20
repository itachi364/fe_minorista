package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_movement")
public class InventoryMovementJpaEntity {

    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private InventoryMovementType movementType;
    @Column(nullable = false)
    private BigDecimal quantity;
    @Column(name = "unit_cost", nullable = false)
    private BigDecimal unitCost;
    @Column(name = "previous_stock", nullable = false)
    private BigDecimal previousStock;
    @Column(name = "resulting_stock", nullable = false)
    private BigDecimal resultingStock;
    @Enumerated(EnumType.STRING)
    @Column(name = "source_document_type", nullable = false)
    private InventorySourceDocumentType sourceDocumentType;
    @Column(name = "source_document_id", nullable = false)
    private UUID sourceDocumentId;
    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;
    @Column(name = "created_by")
    private UUID createdBy;
    @Column(name = "movement_at", nullable = false)
    private Instant movementAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public InventoryMovementType getMovementType() { return movementType; }
    public void setMovementType(InventoryMovementType movementType) { this.movementType = movementType; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public BigDecimal getPreviousStock() { return previousStock; }
    public void setPreviousStock(BigDecimal previousStock) { this.previousStock = previousStock; }
    public BigDecimal getResultingStock() { return resultingStock; }
    public void setResultingStock(BigDecimal resultingStock) { this.resultingStock = resultingStock; }
    public InventorySourceDocumentType getSourceDocumentType() { return sourceDocumentType; }
    public void setSourceDocumentType(InventorySourceDocumentType sourceDocumentType) { this.sourceDocumentType = sourceDocumentType; }
    public UUID getSourceDocumentId() { return sourceDocumentId; }
    public void setSourceDocumentId(UUID sourceDocumentId) { this.sourceDocumentId = sourceDocumentId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public Instant getMovementAt() { return movementAt; }
    public void setMovementAt(Instant movementAt) { this.movementAt = movementAt; }
}
