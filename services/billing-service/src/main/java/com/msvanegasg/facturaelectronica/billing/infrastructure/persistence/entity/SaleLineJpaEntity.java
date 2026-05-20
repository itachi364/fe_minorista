package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sale_line")
public class SaleLineJpaEntity {

    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false)
    private SaleJpaEntity sale;
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    @Column(nullable = false)
    private BigDecimal quantity;
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;
    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;
    @Column(name = "tax_code", nullable = false, length = 50)
    private String taxCode;
    @Column(name = "tax_rate", nullable = false)
    private BigDecimal taxRate;
    @Column(nullable = false)
    private BigDecimal subtotal;
    @Column(name = "tax_amount", nullable = false)
    private BigDecimal taxAmount;
    @Column(nullable = false)
    private BigDecimal total;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public SaleJpaEntity getSale() { return sale; }
    public void setSale(SaleJpaEntity sale) { this.sale = sale; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
