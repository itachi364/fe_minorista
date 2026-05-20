package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "issuer_profile")
public class IssuerProfileJpaEntity {

    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "legal_name", nullable = false, length = 200)
    private String legalName;
    @Column(nullable = false, length = 30)
    private String nit;
    @Column(name = "verification_digit", nullable = false, length = 5)
    private String verificationDigit;
    @Column(name = "tax_responsibilities")
    private String taxResponsibilities;
    @Column(name = "municipality_code", length = 20)
    private String municipalityCode;
    @Column(length = 250)
    private String address;
    @Column(nullable = false)
    private boolean active;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
    public String getVerificationDigit() { return verificationDigit; }
    public void setVerificationDigit(String verificationDigit) { this.verificationDigit = verificationDigit; }
    public String getTaxResponsibilities() { return taxResponsibilities; }
    public void setTaxResponsibilities(String taxResponsibilities) { this.taxResponsibilities = taxResponsibilities; }
    public String getMunicipalityCode() { return municipalityCode; }
    public void setMunicipalityCode(String municipalityCode) { this.municipalityCode = municipalityCode; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
