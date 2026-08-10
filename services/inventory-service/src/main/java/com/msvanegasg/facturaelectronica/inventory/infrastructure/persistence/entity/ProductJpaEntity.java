package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryItemType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
public class ProductJpaEntity {

    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(nullable = false, length = 80)
    private String sku;
    @Column(length = 80)
    private String barcode;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(length = 500)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 30)
    private InventoryItemType itemType;
    @Column(name = "sale_enabled", nullable = false)
    private boolean saleEnabled;
    @Column(name = "purchase_enabled", nullable = false)
    private boolean purchaseEnabled;
    @Column(name = "stock_tracked", nullable = false)
    private boolean stockTracked;
    @Column(name = "sale_price", nullable = false)
    private BigDecimal salePrice;
    @Column(nullable = false)
    private BigDecimal cost;
    @Column(name = "tax_category_code", nullable = false, length = 40)
    private String taxCategoryCode;
    @Column(name = "tax_code", nullable = false, length = 80)
    private String taxCode;
    @Column(name = "tax_label", nullable = false, length = 180)
    private String taxLabel;
    @Column(name = "tax_rate", nullable = false)
    private BigDecimal taxRate;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public InventoryItemType getItemType() { return itemType; }
    public void setItemType(InventoryItemType itemType) { this.itemType = itemType; }
    public boolean isSaleEnabled() { return saleEnabled; }
    public void setSaleEnabled(boolean saleEnabled) { this.saleEnabled = saleEnabled; }
    public boolean isPurchaseEnabled() { return purchaseEnabled; }
    public void setPurchaseEnabled(boolean purchaseEnabled) { this.purchaseEnabled = purchaseEnabled; }
    public boolean isStockTracked() { return stockTracked; }
    public void setStockTracked(boolean stockTracked) { this.stockTracked = stockTracked; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    public String getTaxCategoryCode() { return taxCategoryCode; }
    public void setTaxCategoryCode(String taxCategoryCode) { this.taxCategoryCode = taxCategoryCode; }
    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }
    public String getTaxLabel() { return taxLabel; }
    public void setTaxLabel(String taxLabel) { this.taxLabel = taxLabel; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
