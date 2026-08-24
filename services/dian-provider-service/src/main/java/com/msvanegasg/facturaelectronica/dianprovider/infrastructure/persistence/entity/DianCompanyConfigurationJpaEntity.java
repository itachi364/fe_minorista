package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianConfigurationStatus;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianConnectionMode;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianEnvironment;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianTestStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dian_company_configuration", schema = "dian_provider")
public class DianCompanyConfigurationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false, unique = true)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DianConnectionMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DianEnvironment environment;

    @Column(name = "software_id", length = 120)
    private String softwareId;

    @Column(name = "software_pin_secret_ref", length = 500)
    private String softwarePinSecretRef;

    @Column(name = "technical_key_secret_ref", length = 500)
    private String technicalKeySecretRef;

    @Column(name = "certificate_secret_ref", length = 500)
    private String certificateSecretRef;

    @Column(name = "certificate_alias", length = 180)
    private String certificateAlias;

    @Column(name = "certificate_fingerprint", length = 180)
    private String certificateFingerprint;

    @Column(name = "certificate_expires_at")
    private Instant certificateExpiresAt;

    @Column(name = "service_base_url", length = 500)
    private String serviceBaseUrl;

    @Column(name = "test_set_id", length = 120)
    private String testSetId;

    @Column(name = "accepted_responsibility", nullable = false)
    private boolean acceptedResponsibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DianConfigurationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_test_status", nullable = false, length = 30)
    private DianTestStatus lastTestStatus;

    @Column(name = "last_test_at")
    private Instant lastTestAt;

    @Column(name = "last_test_message", length = 500)
    private String lastTestMessage;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public DianConnectionMode getMode() { return mode; }
    public void setMode(DianConnectionMode mode) { this.mode = mode; }
    public DianEnvironment getEnvironment() { return environment; }
    public void setEnvironment(DianEnvironment environment) { this.environment = environment; }
    public String getSoftwareId() { return softwareId; }
    public void setSoftwareId(String softwareId) { this.softwareId = softwareId; }
    public String getSoftwarePinSecretRef() { return softwarePinSecretRef; }
    public void setSoftwarePinSecretRef(String softwarePinSecretRef) { this.softwarePinSecretRef = softwarePinSecretRef; }
    public String getTechnicalKeySecretRef() { return technicalKeySecretRef; }
    public void setTechnicalKeySecretRef(String technicalKeySecretRef) { this.technicalKeySecretRef = technicalKeySecretRef; }
    public String getCertificateSecretRef() { return certificateSecretRef; }
    public void setCertificateSecretRef(String certificateSecretRef) { this.certificateSecretRef = certificateSecretRef; }
    public String getCertificateAlias() { return certificateAlias; }
    public void setCertificateAlias(String certificateAlias) { this.certificateAlias = certificateAlias; }
    public String getCertificateFingerprint() { return certificateFingerprint; }
    public void setCertificateFingerprint(String certificateFingerprint) { this.certificateFingerprint = certificateFingerprint; }
    public Instant getCertificateExpiresAt() { return certificateExpiresAt; }
    public void setCertificateExpiresAt(Instant certificateExpiresAt) { this.certificateExpiresAt = certificateExpiresAt; }
    public String getServiceBaseUrl() { return serviceBaseUrl; }
    public void setServiceBaseUrl(String serviceBaseUrl) { this.serviceBaseUrl = serviceBaseUrl; }
    public String getTestSetId() { return testSetId; }
    public void setTestSetId(String testSetId) { this.testSetId = testSetId; }
    public boolean isAcceptedResponsibility() { return acceptedResponsibility; }
    public void setAcceptedResponsibility(boolean acceptedResponsibility) { this.acceptedResponsibility = acceptedResponsibility; }
    public DianConfigurationStatus getStatus() { return status; }
    public void setStatus(DianConfigurationStatus status) { this.status = status; }
    public DianTestStatus getLastTestStatus() { return lastTestStatus; }
    public void setLastTestStatus(DianTestStatus lastTestStatus) { this.lastTestStatus = lastTestStatus; }
    public Instant getLastTestAt() { return lastTestAt; }
    public void setLastTestAt(Instant lastTestAt) { this.lastTestAt = lastTestAt; }
    public String getLastTestMessage() { return lastTestMessage; }
    public void setLastTestMessage(String lastTestMessage) { this.lastTestMessage = lastTestMessage; }
    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
