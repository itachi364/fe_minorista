package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity;

import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "numbering_resolution")
public class NumberingResolutionJpaEntity {

    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 40)
    private ElectronicDocumentType documentType;
    @Column(name = "resolution_number", nullable = false, length = 80)
    private String resolutionNumber;
    @Column(nullable = false, length = 4)
    private String prefix;
    @Column(name = "from_number", nullable = false)
    private long fromNumber;
    @Column(name = "to_number", nullable = false)
    private long toNumber;
    @Column(name = "current_number", nullable = false)
    private long currentNumber;
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;
    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FiscalEnvironment environment;
    @Column(nullable = false)
    private boolean active;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public ElectronicDocumentType getDocumentType() { return documentType; }
    public void setDocumentType(ElectronicDocumentType documentType) { this.documentType = documentType; }
    public String getResolutionNumber() { return resolutionNumber; }
    public void setResolutionNumber(String resolutionNumber) { this.resolutionNumber = resolutionNumber; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public long getFromNumber() { return fromNumber; }
    public void setFromNumber(long fromNumber) { this.fromNumber = fromNumber; }
    public long getToNumber() { return toNumber; }
    public void setToNumber(long toNumber) { this.toNumber = toNumber; }
    public long getCurrentNumber() { return currentNumber; }
    public void setCurrentNumber(long currentNumber) { this.currentNumber = currentNumber; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }
    public FiscalEnvironment getEnvironment() { return environment; }
    public void setEnvironment(FiscalEnvironment environment) { this.environment = environment; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
