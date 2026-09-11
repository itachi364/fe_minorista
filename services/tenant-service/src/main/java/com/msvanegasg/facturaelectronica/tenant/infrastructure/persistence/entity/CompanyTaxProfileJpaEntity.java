package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_tax_profile", schema = "tenant")
public class CompanyTaxProfileJpaEntity {
    @Id
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "company_size", length = 30)
    private String companySize;
    @Column(name = "financial_reporting_group", length = 30)
    private String financialReportingGroup;
    @Column(name = "tax_regime", nullable = false, length = 60)
    private String taxRegime;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "company_tax_profile_responsibility", schema = "tenant",
            joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "responsibility_code", nullable = false, length = 30)
    private Set<String> rutResponsibilities = new HashSet<>();
    @Column(name = "vat_responsible", nullable = false)
    private boolean vatResponsible;
    @Column(name = "withholding_agent", nullable = false)
    private boolean withholdingAgent;
    @Column(name = "vat_withholding_agent", nullable = false)
    private boolean vatWithholdingAgent;
    @Column(name = "ica_withholding_agent", nullable = false)
    private boolean icaWithholdingAgent;
    @Column(name = "large_taxpayer", nullable = false)
    private boolean largeTaxpayer;
    @Column(name = "self_withholding", nullable = false)
    private boolean selfWithholding;
    @Column(name = "simple_regime", nullable = false)
    private boolean simpleRegime;
    @Column(name = "ica_municipality_code", length = 10)
    private String icaMunicipalityCode;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "company_tax_profile_ciiu", schema = "tenant",
            joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "ciiu_code", nullable = false, length = 10)
    private Set<String> ciiuCodes = new HashSet<>();
    @Column(name = "updated_by")
    private UUID updatedBy;
    @Column(name = "tax_residency", nullable = false, length = 20)
    private String taxResidency;
    @Column(name = "income_tax_status", nullable = false, length = 20)
    private String incomeTaxStatus;
    @Column(name = "self_withholding_scopes", nullable = false, columnDefinition = "varchar(100)[]")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.ARRAY)
    private String[] selfWithholdingScopes = new String[0];
    @Column(name = "fiscal_evidence_reference", length = 500)
    private String fiscalEvidenceReference;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CompanyTaxProfileJpaEntity() {
    }

    public CompanyTaxProfileJpaEntity(UUID companyId, String companySize, String financialReportingGroup,
            String taxRegime, Set<String> rutResponsibilities, boolean vatResponsible, boolean withholdingAgent,
            boolean vatWithholdingAgent, boolean icaWithholdingAgent, boolean largeTaxpayer, boolean selfWithholding,
            boolean simpleRegime, String icaMunicipalityCode, Set<String> ciiuCodes, UUID updatedBy,
            Instant updatedAt) {
        this(companyId, companySize, financialReportingGroup, taxRegime, rutResponsibilities, vatResponsible,
                withholdingAgent, vatWithholdingAgent, icaWithholdingAgent, largeTaxpayer, selfWithholding,
                simpleRegime, icaMunicipalityCode, ciiuCodes, "UNKNOWN", "UNKNOWN", Set.of(), null, updatedBy,
                updatedAt);
    }

    public CompanyTaxProfileJpaEntity(UUID companyId, String companySize, String financialReportingGroup,
            String taxRegime, Set<String> rutResponsibilities, boolean vatResponsible, boolean withholdingAgent,
            boolean vatWithholdingAgent, boolean icaWithholdingAgent, boolean largeTaxpayer, boolean selfWithholding,
            boolean simpleRegime, String icaMunicipalityCode, Set<String> ciiuCodes, String taxResidency,
            String incomeTaxStatus, Set<String> selfWithholdingScopes, String fiscalEvidenceReference,
            UUID updatedBy, Instant updatedAt) {
        this.companyId = companyId;
        this.companySize = companySize;
        this.financialReportingGroup = financialReportingGroup;
        this.taxRegime = taxRegime;
        this.rutResponsibilities = new HashSet<>(rutResponsibilities);
        this.vatResponsible = vatResponsible;
        this.withholdingAgent = withholdingAgent;
        this.vatWithholdingAgent = vatWithholdingAgent;
        this.icaWithholdingAgent = icaWithholdingAgent;
        this.largeTaxpayer = largeTaxpayer;
        this.selfWithholding = selfWithholding;
        this.simpleRegime = simpleRegime;
        this.icaMunicipalityCode = icaMunicipalityCode;
        this.ciiuCodes = new HashSet<>(ciiuCodes);
        this.taxResidency = taxResidency;
        this.incomeTaxStatus = incomeTaxStatus;
        this.selfWithholdingScopes = selfWithholdingScopes.toArray(String[]::new);
        this.fiscalEvidenceReference = fiscalEvidenceReference;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public UUID getCompanyId() { return companyId; }
    public String getCompanySize() { return companySize; }
    public String getFinancialReportingGroup() { return financialReportingGroup; }
    public String getTaxRegime() { return taxRegime; }
    public Set<String> getRutResponsibilities() { return rutResponsibilities; }
    public boolean isVatResponsible() { return vatResponsible; }
    public boolean isWithholdingAgent() { return withholdingAgent; }
    public boolean isVatWithholdingAgent() { return vatWithholdingAgent; }
    public boolean isIcaWithholdingAgent() { return icaWithholdingAgent; }
    public boolean isLargeTaxpayer() { return largeTaxpayer; }
    public boolean isSelfWithholding() { return selfWithholding; }
    public boolean isSimpleRegime() { return simpleRegime; }
    public String getIcaMunicipalityCode() { return icaMunicipalityCode; }
    public Set<String> getCiiuCodes() { return ciiuCodes; }
    public String getTaxResidency() { return taxResidency; }
    public String getIncomeTaxStatus() { return incomeTaxStatus; }
    public Set<String> getSelfWithholdingScopes() { return Set.of(selfWithholdingScopes); }
    public String getFiscalEvidenceReference() { return fiscalEvidenceReference; }
    public UUID getUpdatedBy() { return updatedBy; }
    public Instant getUpdatedAt() { return updatedAt; }
}
