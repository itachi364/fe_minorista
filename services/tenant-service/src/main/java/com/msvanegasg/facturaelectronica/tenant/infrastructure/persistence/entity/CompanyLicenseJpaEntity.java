package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicenseStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_license", schema = "tenant")
public class CompanyLicenseJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "plan_code", nullable = false, length = 60)
    private String planCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompanyLicenseStatus status;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_monthly_documents")
    private Integer maxMonthlyDocuments;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "enabled_modules", nullable = false, columnDefinition = "text[]")
    private String[] enabledModules = new String[0];

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CompanyLicenseJpaEntity() {
    }

    public CompanyLicenseJpaEntity(UUID id, UUID companyId, String planCode, CompanyLicenseStatus status,
            LocalDate validFrom, LocalDate validTo, Integer maxUsers, Integer maxMonthlyDocuments,
            String[] enabledModules, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.planCode = planCode;
        this.status = status;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.maxUsers = maxUsers;
        this.maxMonthlyDocuments = maxMonthlyDocuments;
        this.enabledModules = enabledModules == null ? new String[0] : enabledModules.clone();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getPlanCode() {
        return planCode;
    }

    public CompanyLicenseStatus getStatus() {
        return status;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }

    public Integer getMaxMonthlyDocuments() {
        return maxMonthlyDocuments;
    }

    public String[] getEnabledModules() {
        return enabledModules == null ? new String[0] : enabledModules.clone();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
