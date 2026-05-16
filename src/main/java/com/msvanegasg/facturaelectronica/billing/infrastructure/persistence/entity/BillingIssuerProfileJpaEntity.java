package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "billing_issuer_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingIssuerProfileJpaEntity {

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
    private Boolean active;
}
