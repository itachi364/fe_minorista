package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianSubmissionEventStatus;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianSubmissionEventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dian_submission_event", schema = "dian_provider")
public class DianSubmissionEventJpaEntity {

    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;
    @Column(name = "document_id", nullable = false)
    private UUID documentId;
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private DianSubmissionEventType eventType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DianSubmissionEventStatus status;
    @Column(name = "dian_code")
    private String dianCode;
    @Column(name = "dian_message")
    private String dianMessage;
    @Column(name = "correlation_id")
    private String correlationId;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public UUID getSubmissionId() { return submissionId; }
    public void setSubmissionId(UUID submissionId) { this.submissionId = submissionId; }
    public UUID getDocumentId() { return documentId; }
    public void setDocumentId(UUID documentId) { this.documentId = documentId; }
    public DianSubmissionEventType getEventType() { return eventType; }
    public void setEventType(DianSubmissionEventType eventType) { this.eventType = eventType; }
    public DianSubmissionEventStatus getStatus() { return status; }
    public void setStatus(DianSubmissionEventStatus status) { this.status = status; }
    public String getDianCode() { return dianCode; }
    public void setDianCode(String dianCode) { this.dianCode = dianCode; }
    public String getDianMessage() { return dianMessage; }
    public void setDianMessage(String dianMessage) { this.dianMessage = dianMessage; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
