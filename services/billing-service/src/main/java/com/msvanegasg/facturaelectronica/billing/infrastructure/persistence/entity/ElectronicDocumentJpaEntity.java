package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "electronic_document")
public class ElectronicDocumentJpaEntity {

    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false)
    private SaleJpaEntity sale;
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private ElectronicDocumentType documentType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ElectronicDocumentStatus status;
    @Enumerated(EnumType.STRING)
    @Column(name = "provider_status", nullable = false)
    private ProviderStatus providerStatus;
    @Column(nullable = false, length = 10)
    private String prefix;
    @Column(name = "document_number", nullable = false)
    private long documentNumber;
    @Column(name = "cufe_cude", nullable = false, length = 200)
    private String cufeCude;
    @Column(name = "qr_content", nullable = false)
    private String qrContent;
    @Column(nullable = false)
    private BigDecimal subtotal;
    @Column(name = "tax_total", nullable = false)
    private BigDecimal taxTotal;
    @Column(nullable = false)
    private BigDecimal total;
    @Column(name = "provider_tracking_id", length = 120)
    private String providerTrackingId;
    @Column(name = "provider_error_code", length = 80)
    private String providerErrorCode;
    @Column(name = "provider_error_message")
    private String providerErrorMessage;
    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;
    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;
    @Column(name = "inventory_applied_at")
    private Instant inventoryAppliedAt;
    @Column(name = "accounting_applied_at")
    private Instant accountingAppliedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public SaleJpaEntity getSale() { return sale; }
    public void setSale(SaleJpaEntity sale) { this.sale = sale; }
    public ElectronicDocumentType getDocumentType() { return documentType; }
    public void setDocumentType(ElectronicDocumentType documentType) { this.documentType = documentType; }
    public ElectronicDocumentStatus getStatus() { return status; }
    public void setStatus(ElectronicDocumentStatus status) { this.status = status; }
    public ProviderStatus getProviderStatus() { return providerStatus; }
    public void setProviderStatus(ProviderStatus providerStatus) { this.providerStatus = providerStatus; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public long getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(long documentNumber) { this.documentNumber = documentNumber; }
    public String getCufeCude() { return cufeCude; }
    public void setCufeCude(String cufeCude) { this.cufeCude = cufeCude; }
    public String getQrContent() { return qrContent; }
    public void setQrContent(String qrContent) { this.qrContent = qrContent; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getTaxTotal() { return taxTotal; }
    public void setTaxTotal(BigDecimal taxTotal) { this.taxTotal = taxTotal; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getProviderTrackingId() { return providerTrackingId; }
    public void setProviderTrackingId(String providerTrackingId) { this.providerTrackingId = providerTrackingId; }
    public String getProviderErrorCode() { return providerErrorCode; }
    public void setProviderErrorCode(String providerErrorCode) { this.providerErrorCode = providerErrorCode; }
    public String getProviderErrorMessage() { return providerErrorMessage; }
    public void setProviderErrorMessage(String providerErrorMessage) { this.providerErrorMessage = providerErrorMessage; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }
    public Instant getInventoryAppliedAt() { return inventoryAppliedAt; }
    public void setInventoryAppliedAt(Instant inventoryAppliedAt) { this.inventoryAppliedAt = inventoryAppliedAt; }
    public Instant getAccountingAppliedAt() { return accountingAppliedAt; }
    public void setAccountingAppliedAt(Instant accountingAppliedAt) { this.accountingAppliedAt = accountingAppliedAt; }
}
