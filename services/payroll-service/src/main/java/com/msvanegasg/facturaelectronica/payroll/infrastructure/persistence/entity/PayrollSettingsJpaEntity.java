package com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payroll_settings")
public class PayrollSettingsJpaEntity {
    @Id
    @Column(name = "company_id")
    private UUID companyId;
    @Column(name = "electronic_payroll_enabled", nullable = false)
    private boolean electronicPayrollEnabled;
    @Column(name = "provider_mode", nullable = false, length = 30)
    private String providerMode;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public boolean isElectronicPayrollEnabled() { return electronicPayrollEnabled; }
    public void setElectronicPayrollEnabled(boolean electronicPayrollEnabled) { this.electronicPayrollEnabled = electronicPayrollEnabled; }
    public String getProviderMode() { return providerMode; }
    public void setProviderMode(String providerMode) { this.providerMode = providerMode; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
