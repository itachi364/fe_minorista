package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "company", schema = "tenant")
public class CompanyJpaEntity {

    @Id
    private UUID id;

    @Column(name = "legal_name", nullable = false, length = 180)
    private String legalName;

    @Column(name = "trade_name", length = 180)
    private String tradeName;

    @Column(name = "identification_type_id", nullable = false)
    private UUID identificationTypeId;

    @Column(name = "identification_number", nullable = false, length = 30)
    private String identificationNumber;

    @Column(name = "verification_digit", length = 2)
    private String verificationDigit;

    @Column(nullable = false, length = 180)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompanyStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CompanyJpaEntity() {
    }

    public CompanyJpaEntity(UUID id, String legalName, String tradeName, UUID identificationTypeId,
            String identificationNumber, String verificationDigit, String email, CompanyStatus status,
            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.legalName = legalName;
        this.tradeName = tradeName;
        this.identificationTypeId = identificationTypeId;
        this.identificationNumber = identificationNumber;
        this.verificationDigit = verificationDigit;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getLegalName() {
        return legalName;
    }

    public String getTradeName() {
        return tradeName;
    }

    public UUID getIdentificationTypeId() {
        return identificationTypeId;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public String getVerificationDigit() {
        return verificationDigit;
    }

    public String getEmail() {
        return email;
    }

    public CompanyStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
