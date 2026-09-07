package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "withholding_rule")
public class WithholdingRuleJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "rule_set_version", nullable = false, length = 40)
    private String ruleSetVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 40)
    private FiscalOperationType operationType;

    @Column(name = "concept_code", length = 80)
    private String conceptCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "withholding_type", nullable = false, length = 30)
    private WithholdingType withholdingType;

    @Column(name = "base_min_amount", nullable = false)
    private BigDecimal baseMinAmount;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal rate;

    @Column(name = "requires_company_withholding_agent", nullable = false)
    private Boolean requiresCompanyWithholdingAgent;

    @Column(name = "requires_company_vat_responsible", nullable = false)
    private Boolean requiresCompanyVatResponsible;

    @Column(name = "required_third_party_tax_regime", length = 40)
    private String requiredThirdPartyTaxRegime;

    @Column(name = "required_third_party_responsibility", length = 20)
    private String requiredThirdPartyResponsibility;

    @Column(name = "municipality_code", length = 20)
    private String municipalityCode;

    @Column(name = "ciiu_code", length = 10)
    private String ciiuCode;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(nullable = false)
    private Integer priority;

    @Column(nullable = false)
    private Boolean active;
}
