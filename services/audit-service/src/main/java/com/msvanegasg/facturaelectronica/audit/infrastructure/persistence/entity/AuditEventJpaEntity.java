package com.msvanegasg.facturaelectronica.audit.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.msvanegasg.facturaelectronica.audit.domain.model.AuditResult;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_event")
public class AuditEventJpaEntity {

    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;
    @Column(name = "resource_type", nullable = false, length = 80)
    private String resourceType;
    @Column(name = "resource_id", length = 120)
    private String resourceId;
    @Column(nullable = false, length = 80)
    private String action;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AuditResult result;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String detail;
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public AuditResult getResult() { return result; }
    public void setResult(AuditResult result) { this.result = result; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
