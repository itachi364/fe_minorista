package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.BuyerIdentificationMode;
import com.msvanegasg.facturaelectronica.billing.domain.model.PaymentMethodCode;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.VirtualWalletCode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sale")
public class SaleJpaEntity {

    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "customer_id")
    private UUID customerId;
    @Enumerated(EnumType.STRING)
    @Column(name = "buyer_identification_mode", nullable = false, length = 30)
    private BuyerIdentificationMode buyerIdentificationMode;
    @Column(name = "payment_method_id")
    private UUID paymentMethodId;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method_code", nullable = false, length = 30)
    private PaymentMethodCode paymentMethodCode;
    @Enumerated(EnumType.STRING)
    @Column(name = "virtual_wallet_code", length = 50)
    private VirtualWalletCode virtualWalletCode;
    @Enumerated(EnumType.STRING)
    @Column(name = "sale_channel", nullable = false)
    private SaleChannel saleChannel;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleStatus status;
    @Column(nullable = false)
    private BigDecimal subtotal;
    @Column(name = "discount_total", nullable = false)
    private BigDecimal discountTotal;
    @Column(name = "tax_total", nullable = false)
    private BigDecimal taxTotal;
    @Column(nullable = false)
    private BigDecimal total;
    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;
    @Column(name = "created_by")
    private UUID createdBy;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "confirmed_at")
    private Instant confirmedAt;
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleLineJpaEntity> lines = new ArrayList<>();
    @OneToOne(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private ElectronicDocumentJpaEntity electronicDocument;

    public void replaceLines(List<SaleLineJpaEntity> newLines) {
        lines.clear();
        newLines.forEach(line -> {
            line.setSale(this);
            lines.add(line);
        });
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public BuyerIdentificationMode getBuyerIdentificationMode() { return buyerIdentificationMode; }
    public void setBuyerIdentificationMode(BuyerIdentificationMode buyerIdentificationMode) { this.buyerIdentificationMode = buyerIdentificationMode; }
    public UUID getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(UUID paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public PaymentMethodCode getPaymentMethodCode() { return paymentMethodCode; }
    public void setPaymentMethodCode(PaymentMethodCode paymentMethodCode) { this.paymentMethodCode = paymentMethodCode; }
    public VirtualWalletCode getVirtualWalletCode() { return virtualWalletCode; }
    public void setVirtualWalletCode(VirtualWalletCode virtualWalletCode) { this.virtualWalletCode = virtualWalletCode; }
    public SaleChannel getSaleChannel() { return saleChannel; }
    public void setSaleChannel(SaleChannel saleChannel) { this.saleChannel = saleChannel; }
    public SaleStatus getStatus() { return status; }
    public void setStatus(SaleStatus status) { this.status = status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscountTotal() { return discountTotal; }
    public void setDiscountTotal(BigDecimal discountTotal) { this.discountTotal = discountTotal; }
    public BigDecimal getTaxTotal() { return taxTotal; }
    public void setTaxTotal(BigDecimal taxTotal) { this.taxTotal = taxTotal; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(Instant confirmedAt) { this.confirmedAt = confirmedAt; }
    public List<SaleLineJpaEntity> getLines() { return lines; }
    public ElectronicDocumentJpaEntity getElectronicDocument() { return electronicDocument; }
    public void setElectronicDocument(ElectronicDocumentJpaEntity electronicDocument) {
        this.electronicDocument = electronicDocument;
        if (electronicDocument != null) {
            electronicDocument.setSale(this);
        }
    }
}
