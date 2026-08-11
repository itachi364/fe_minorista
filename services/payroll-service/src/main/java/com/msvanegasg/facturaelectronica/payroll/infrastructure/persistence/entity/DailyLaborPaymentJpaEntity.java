package com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "daily_labor_payment")
public class DailyLaborPaymentJpaEntity {
    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "worker_id", nullable = false)
    private UUID workerId;
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;
    @Column(name = "activity_description", nullable = false, length = 300)
    private String activityDescription;
    @Column(name = "agreed_amount", nullable = false)
    private BigDecimal agreedAmount;
    @Column(name = "paid_amount", nullable = false)
    private BigDecimal paidAmount;
    @Column(name = "payment_method_code", nullable = false, length = 40)
    private String paymentMethodCode;
    @Column(name = "legal_notice_accepted", nullable = false)
    private boolean legalNoticeAccepted;
    @Column(length = 500)
    private String notes;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public UUID getWorkerId() { return workerId; }
    public void setWorkerId(UUID workerId) { this.workerId = workerId; }
    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
    public String getActivityDescription() { return activityDescription; }
    public void setActivityDescription(String activityDescription) { this.activityDescription = activityDescription; }
    public BigDecimal getAgreedAmount() { return agreedAmount; }
    public void setAgreedAmount(BigDecimal agreedAmount) { this.agreedAmount = agreedAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public String getPaymentMethodCode() { return paymentMethodCode; }
    public void setPaymentMethodCode(String paymentMethodCode) { this.paymentMethodCode = paymentMethodCode; }
    public boolean isLegalNoticeAccepted() { return legalNoticeAccepted; }
    public void setLegalNoticeAccepted(boolean legalNoticeAccepted) { this.legalNoticeAccepted = legalNoticeAccepted; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
