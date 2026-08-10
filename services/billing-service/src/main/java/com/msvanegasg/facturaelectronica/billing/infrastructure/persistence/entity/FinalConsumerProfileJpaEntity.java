package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "final_consumer_profile")
public class FinalConsumerProfileJpaEntity {

    @Id
    private UUID id;
    @Column(name = "company_id")
    private UUID companyId;
    @Column(name = "profile_code", nullable = false, length = 40)
    private String profileCode;
    @Column(name = "identification_type_code", nullable = false)
    private int identificationTypeCode;
    @Column(name = "identification_number", nullable = false, length = 30)
    private String identificationNumber;
    @Column(name = "display_name", nullable = false, length = 180)
    private String displayName;
    @Column(nullable = false)
    private boolean active;
    @Column(nullable = false, length = 80)
    private String source;
    @Column(name = "source_version", nullable = false, length = 40)
    private String sourceVersion;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public String getProfileCode() { return profileCode; }
    public void setProfileCode(String profileCode) { this.profileCode = profileCode; }
    public int getIdentificationTypeCode() { return identificationTypeCode; }
    public void setIdentificationTypeCode(int identificationTypeCode) { this.identificationTypeCode = identificationTypeCode; }
    public String getIdentificationNumber() { return identificationNumber; }
    public void setIdentificationNumber(String identificationNumber) { this.identificationNumber = identificationNumber; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getSourceVersion() { return sourceVersion; }
    public void setSourceVersion(String sourceVersion) { this.sourceVersion = sourceVersion; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
