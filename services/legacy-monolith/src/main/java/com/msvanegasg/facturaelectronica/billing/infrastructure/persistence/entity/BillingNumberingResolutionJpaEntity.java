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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "billing_numbering_resolution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingNumberingResolutionJpaEntity {

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
    private Long fromNumber;

    @Column(name = "to_number", nullable = false)
    private Long toNumber;

    @Column(name = "current_number", nullable = false)
    private Long currentNumber;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FiscalEnvironment environment;

    @Column(nullable = false)
    private Boolean active;
}
