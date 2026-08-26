package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_fiscal_policy")
public class CompanyFiscalPolicyJpaEntity {

    @Id
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Enumerated(EnumType.STRING)
    @Column(name = "default_sale_document_type", nullable = false, length = 40)
    private ElectronicDocumentType defaultSaleDocumentType;
    @Column(name = "allow_document_type_override", nullable = false)
    private boolean allowDocumentTypeOverride;
    @Column(name = "require_pin_for_override", nullable = false)
    private boolean requirePinForOverride;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public ElectronicDocumentType getDefaultSaleDocumentType() { return defaultSaleDocumentType; }
    public void setDefaultSaleDocumentType(ElectronicDocumentType defaultSaleDocumentType) { this.defaultSaleDocumentType = defaultSaleDocumentType; }
    public boolean isAllowDocumentTypeOverride() { return allowDocumentTypeOverride; }
    public void setAllowDocumentTypeOverride(boolean allowDocumentTypeOverride) { this.allowDocumentTypeOverride = allowDocumentTypeOverride; }
    public boolean isRequirePinForOverride() { return requirePinForOverride; }
    public void setRequirePinForOverride(boolean requirePinForOverride) { this.requirePinForOverride = requirePinForOverride; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
