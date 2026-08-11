package com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "worker")
public class WorkerJpaEntity {
    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "identification_type_code", nullable = false)
    private Short identificationTypeCode;
    @Column(name = "identification_number", nullable = false, length = 40)
    private String identificationNumber;
    @Column(name = "verification_digit")
    private Short verificationDigit;
    @Column(name = "full_name", nullable = false, length = 180)
    private String fullName;
    @Column(name = "worker_classification", nullable = false, length = 40)
    private String workerClassification;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public Short getIdentificationTypeCode() { return identificationTypeCode; }
    public void setIdentificationTypeCode(Short identificationTypeCode) { this.identificationTypeCode = identificationTypeCode; }
    public String getIdentificationNumber() { return identificationNumber; }
    public void setIdentificationNumber(String identificationNumber) { this.identificationNumber = identificationNumber; }
    public Short getVerificationDigit() { return verificationDigit; }
    public void setVerificationDigit(Short verificationDigit) { this.verificationDigit = verificationDigit; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getWorkerClassification() { return workerClassification; }
    public void setWorkerClassification(String workerClassification) { this.workerClassification = workerClassification; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
