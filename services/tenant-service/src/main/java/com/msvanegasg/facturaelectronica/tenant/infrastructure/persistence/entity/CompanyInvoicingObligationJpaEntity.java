package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.InvoicingObligationStatus;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_invoicing_obligation_snapshot", schema = "tenant")
public class CompanyInvoicingObligationJpaEntity {
    @Id private UUID id;
    @Column(name = "company_id", nullable = false) private UUID companyId;
    @Column(nullable = false) private long version;
    @Column(name = "current_snapshot", nullable = false) private boolean current;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private InvoicingObligationStatus status;
    @Column(name = "decision_code", nullable = false, length = 100) private String decisionCode;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "company_invoicing_obligation_reason", schema = "tenant", joinColumns = @JoinColumn(name = "snapshot_id"))
    @OrderColumn(name = "reason_order") @Column(name = "reason_code", nullable = false, length = 100)
    private List<String> decisionReasons = new ArrayList<>();
    @Column(name = "person_type", length = 30) private String personType;
    @Column(name = "tax_regime", length = 60) private String taxRegime;
    @Column(name = "rut_generated_at") private LocalDate rutGeneratedAt;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "company_invoicing_obligation_responsibility", schema = "tenant", joinColumns = @JoinColumn(name = "snapshot_id"))
    @Column(name = "responsibility_code", nullable = false, length = 30)
    private Set<String> rutResponsibilityCodes = new HashSet<>();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "company_invoicing_obligation_ciiu", schema = "tenant", joinColumns = @JoinColumn(name = "snapshot_id"))
    @Column(name = "ciiu_code", nullable = false, length = 10)
    private Set<String> ciiuCodes = new HashSet<>();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "company_invoicing_obligation_operation", schema = "tenant", joinColumns = @JoinColumn(name = "snapshot_id"))
    @Column(name = "operation_type", nullable = false, length = 60)
    private Set<String> economicOperationTypes = new HashSet<>();
    @Column(name = "customs_user") private Boolean customsUser;
    @Column(name = "establishment_count") private Integer establishmentCount;
    @Column(name = "exploits_intangibles") private Boolean exploitsIntangibles;
    @Column(name = "only_excluded_or_untaxed_operations") private Boolean onlyExcludedOrUntaxedOperations;
    @Column(name = "previous_year_gross_activity_income") private BigDecimal previousYearGrossActivityIncome;
    @Column(name = "current_year_gross_activity_income") private BigDecimal currentYearGrossActivityIncome;
    @Column(name = "previous_year_taxed_financial_operations") private BigDecimal previousYearTaxedActivityFinancialOperations;
    @Column(name = "current_year_taxed_financial_operations") private BigDecimal currentYearTaxedActivityFinancialOperations;
    @Column(name = "largest_previous_year_taxed_contract") private BigDecimal largestPreviousYearTaxedContract;
    @Column(name = "largest_current_year_taxed_contract") private BigDecimal largestCurrentYearTaxedContract;
    @Column(name = "largest_same_customer_aggregate") private BigDecimal largestSameCustomerAggregate;
    @Column(name = "voluntary_electronic_invoicer") private Boolean voluntaryElectronicInvoicer;
    @Column(name = "special_exception_type", length = 80) private String specialExceptionType;
    @Column(name = "special_exception_scope", length = 240) private String specialExceptionScope;
    @Column(name = "rut_asset_id") private UUID rutAssetId;
    @Column(name = "uvt_value", nullable = false) private BigDecimal uvtValue;
    @Column(name = "normative_rule_set_version", nullable = false, length = 80) private String normativeRuleSetVersion;
    @Column(name = "evaluated_by") private UUID evaluatedBy;
    @Column(name = "evaluated_at", nullable = false) private Instant evaluatedAt;

    public CompanyInvoicingObligationJpaEntity() {}

    public UUID getId() { return id; } public void setId(UUID value) { id = value; }
    public UUID getCompanyId() { return companyId; } public void setCompanyId(UUID value) { companyId = value; }
    public long getVersion() { return version; } public void setVersion(long value) { version = value; }
    public boolean isCurrent() { return current; } public void setCurrent(boolean value) { current = value; }
    public InvoicingObligationStatus getStatus() { return status; } public void setStatus(InvoicingObligationStatus value) { status = value; }
    public String getDecisionCode() { return decisionCode; } public void setDecisionCode(String value) { decisionCode = value; }
    public List<String> getDecisionReasons() { return decisionReasons; } public void setDecisionReasons(List<String> value) { decisionReasons = new ArrayList<>(value); }
    public String getPersonType() { return personType; } public void setPersonType(String value) { personType = value; }
    public String getTaxRegime() { return taxRegime; } public void setTaxRegime(String value) { taxRegime = value; }
    public LocalDate getRutGeneratedAt() { return rutGeneratedAt; } public void setRutGeneratedAt(LocalDate value) { rutGeneratedAt = value; }
    public Set<String> getRutResponsibilityCodes() { return rutResponsibilityCodes; } public void setRutResponsibilityCodes(Set<String> value) { rutResponsibilityCodes = new HashSet<>(value); }
    public Set<String> getCiiuCodes() { return ciiuCodes; } public void setCiiuCodes(Set<String> value) { ciiuCodes = new HashSet<>(value); }
    public Set<String> getEconomicOperationTypes() { return economicOperationTypes; } public void setEconomicOperationTypes(Set<String> value) { economicOperationTypes = new HashSet<>(value); }
    public Boolean getCustomsUser() { return customsUser; } public void setCustomsUser(Boolean value) { customsUser = value; }
    public Integer getEstablishmentCount() { return establishmentCount; } public void setEstablishmentCount(Integer value) { establishmentCount = value; }
    public Boolean getExploitsIntangibles() { return exploitsIntangibles; } public void setExploitsIntangibles(Boolean value) { exploitsIntangibles = value; }
    public Boolean getOnlyExcludedOrUntaxedOperations() { return onlyExcludedOrUntaxedOperations; } public void setOnlyExcludedOrUntaxedOperations(Boolean value) { onlyExcludedOrUntaxedOperations = value; }
    public BigDecimal getPreviousYearGrossActivityIncome() { return previousYearGrossActivityIncome; } public void setPreviousYearGrossActivityIncome(BigDecimal value) { previousYearGrossActivityIncome = value; }
    public BigDecimal getCurrentYearGrossActivityIncome() { return currentYearGrossActivityIncome; } public void setCurrentYearGrossActivityIncome(BigDecimal value) { currentYearGrossActivityIncome = value; }
    public BigDecimal getPreviousYearTaxedActivityFinancialOperations() { return previousYearTaxedActivityFinancialOperations; } public void setPreviousYearTaxedActivityFinancialOperations(BigDecimal value) { previousYearTaxedActivityFinancialOperations = value; }
    public BigDecimal getCurrentYearTaxedActivityFinancialOperations() { return currentYearTaxedActivityFinancialOperations; } public void setCurrentYearTaxedActivityFinancialOperations(BigDecimal value) { currentYearTaxedActivityFinancialOperations = value; }
    public BigDecimal getLargestPreviousYearTaxedContract() { return largestPreviousYearTaxedContract; } public void setLargestPreviousYearTaxedContract(BigDecimal value) { largestPreviousYearTaxedContract = value; }
    public BigDecimal getLargestCurrentYearTaxedContract() { return largestCurrentYearTaxedContract; } public void setLargestCurrentYearTaxedContract(BigDecimal value) { largestCurrentYearTaxedContract = value; }
    public BigDecimal getLargestSameCustomerAggregate() { return largestSameCustomerAggregate; } public void setLargestSameCustomerAggregate(BigDecimal value) { largestSameCustomerAggregate = value; }
    public Boolean getVoluntaryElectronicInvoicer() { return voluntaryElectronicInvoicer; } public void setVoluntaryElectronicInvoicer(Boolean value) { voluntaryElectronicInvoicer = value; }
    public String getSpecialExceptionType() { return specialExceptionType; } public void setSpecialExceptionType(String value) { specialExceptionType = value; }
    public String getSpecialExceptionScope() { return specialExceptionScope; } public void setSpecialExceptionScope(String value) { specialExceptionScope = value; }
    public UUID getRutAssetId() { return rutAssetId; } public void setRutAssetId(UUID value) { rutAssetId = value; }
    public BigDecimal getUvtValue() { return uvtValue; } public void setUvtValue(BigDecimal value) { uvtValue = value; }
    public String getNormativeRuleSetVersion() { return normativeRuleSetVersion; } public void setNormativeRuleSetVersion(String value) { normativeRuleSetVersion = value; }
    public UUID getEvaluatedBy() { return evaluatedBy; } public void setEvaluatedBy(UUID value) { evaluatedBy = value; }
    public Instant getEvaluatedAt() { return evaluatedAt; } public void setEvaluatedAt(Instant value) { evaluatedAt = value; }
}
