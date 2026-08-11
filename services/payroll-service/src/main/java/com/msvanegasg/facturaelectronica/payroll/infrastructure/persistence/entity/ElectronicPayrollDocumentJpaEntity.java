package com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "electronic_payroll_document")
public class ElectronicPayrollDocumentJpaEntity {
    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "daily_labor_payment_id", nullable = false)
    private UUID dailyLaborPaymentId;
    @Column(nullable = false, length = 120)
    private String cune;
    @Column(nullable = false, length = 30)
    private String status;
    @Column(name = "provider_response", length = 500)
    private String providerResponse;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public UUID getDailyLaborPaymentId() { return dailyLaborPaymentId; }
    public void setDailyLaborPaymentId(UUID dailyLaborPaymentId) { this.dailyLaborPaymentId = dailyLaborPaymentId; }
    public String getCune() { return cune; }
    public void setCune(String cune) { this.cune = cune; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
