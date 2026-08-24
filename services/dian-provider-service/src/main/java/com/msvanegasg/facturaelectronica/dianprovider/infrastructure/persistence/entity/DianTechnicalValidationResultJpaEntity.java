package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianValidationStatus;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianValidationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dian_technical_validation_result", schema = "dian_provider")
public class DianTechnicalValidationResultJpaEntity {

    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;
    @Column(name = "document_id", nullable = false)
    private UUID documentId;
    @Enumerated(EnumType.STRING)
    @Column(name = "validation_type", nullable = false)
    private DianValidationType validationType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DianValidationStatus result;
    @Column(name = "rule_code")
    private String ruleCode;
    @Column(name = "message")
    private String message;
    @Column(name = "source_version", nullable = false)
    private String sourceVersion;
    @Column(name = "validated_at", nullable = false)
    private Instant validatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public UUID getSubmissionId() { return submissionId; }
    public void setSubmissionId(UUID submissionId) { this.submissionId = submissionId; }
    public UUID getDocumentId() { return documentId; }
    public void setDocumentId(UUID documentId) { this.documentId = documentId; }
    public DianValidationType getValidationType() { return validationType; }
    public void setValidationType(DianValidationType validationType) { this.validationType = validationType; }
    public DianValidationStatus getResult() { return result; }
    public void setResult(DianValidationStatus result) { this.result = result; }
    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSourceVersion() { return sourceVersion; }
    public void setSourceVersion(String sourceVersion) { this.sourceVersion = sourceVersion; }
    public Instant getValidatedAt() { return validatedAt; }
    public void setValidatedAt(Instant validatedAt) { this.validatedAt = validatedAt; }
}
