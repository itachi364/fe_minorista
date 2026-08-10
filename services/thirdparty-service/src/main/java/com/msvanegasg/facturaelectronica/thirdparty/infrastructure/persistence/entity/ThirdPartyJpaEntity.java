package com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.entity;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.thirdparty.domain.model.PersonType;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.TaxRegime;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdPartyRole;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "third_party")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class ThirdPartyJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "person_type", nullable = false, length = 20)
    private PersonType personType;

    @Column(name = "identification_type_code", nullable = false)
    private Integer identificationTypeCode;

    @Column(name = "identification_number", nullable = false, length = 30)
    private String identificationNumber;

    @Column(name = "verification_digit")
    private Integer verificationDigit;

    @Column(name = "full_name", length = 220)
    private String fullName;

    @Column(name = "business_name", length = 220)
    private String businessName;

    @Column(name = "trade_name", length = 220)
    private String tradeName;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "address", length = 250)
    private String address;

    @Column(name = "municipality_code", length = 20)
    private String municipalityCode;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "third_party_tax_responsibility", joinColumns = @JoinColumn(name = "third_party_id"))
    @Column(name = "tax_responsibility_code", nullable = false, length = 20)
    @Builder.Default
    private Set<String> taxResponsibilities = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_regime", length = 30)
    private TaxRegime taxRegime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "third_party_role", joinColumns = @JoinColumn(name = "third_party_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    @Builder.Default
    private Set<ThirdPartyRole> roles = new LinkedHashSet<>();

    @Column(name = "active", nullable = false)
    private Boolean active;
}
