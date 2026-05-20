package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_balance")
@IdClass(StockBalanceId.class)
public class StockBalanceJpaEntity {

    @Id
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Id
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    @Column(name = "current_stock", nullable = false)
    private BigDecimal currentStock;
    @Column(name = "reserved_stock", nullable = false)
    private BigDecimal reservedStock;
    @Column(name = "average_cost", nullable = false)
    private BigDecimal averageCost;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public BigDecimal getCurrentStock() { return currentStock; }
    public void setCurrentStock(BigDecimal currentStock) { this.currentStock = currentStock; }
    public BigDecimal getReservedStock() { return reservedStock; }
    public void setReservedStock(BigDecimal reservedStock) { this.reservedStock = reservedStock; }
    public BigDecimal getAverageCost() { return averageCost; }
    public void setAverageCost(BigDecimal averageCost) { this.averageCost = averageCost; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
