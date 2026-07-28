package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.AccessAuditResult;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "identity_access_audit", schema = "identity")
public class AccessAuditJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(name = "resource_type", nullable = false, length = 80)
    private String resourceType;

    @Column(name = "resource_id", length = 120)
    private String resourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AccessAuditResult result;

    @Column(length = 500)
    private String detail;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public AccessAuditJpaEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public AccessAuditResult getResult() { return result; }
    public void setResult(AccessAuditResult result) { this.result = result; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
