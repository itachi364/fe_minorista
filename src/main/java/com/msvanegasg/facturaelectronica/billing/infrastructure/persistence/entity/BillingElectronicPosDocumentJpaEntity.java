package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;

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
@Table(name = "billing_electronic_pos_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingElectronicPosDocumentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "sale_id")
    private UUID saleId;

    @Column(name = "buyer_name", length = 200)
    private String buyerName;

    @Column(name = "buyer_document_type", length = 30)
    private String buyerDocumentType;

    @Column(name = "buyer_document_number", length = 50)
    private String buyerDocumentNumber;

    @Column(nullable = false, length = 4)
    private String prefix;

    @Column(name = "document_number", nullable = false)
    private Long documentNumber;

    @Column(nullable = false, length = 200)
    private String cude;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(name = "tax_total", nullable = false)
    private BigDecimal taxTotal;

    @Column(nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ElectronicDocumentStatus status;

    @Column(name = "issue_at", nullable = false)
    private Instant issueAt;

    @Column(name = "provider_submission_id", length = 120)
    private String providerSubmissionId;

    @Column(name = "provider_cufe_cude", length = 200)
    private String providerCufeCude;

    @Column(name = "provider_qr_content")
    private String providerQrContent;

    @Column(name = "provider_xml_content")
    private String providerXmlContent;

    @Column(name = "provider_graphic_representation_content")
    private String providerGraphicRepresentationContent;

    @Column(name = "provider_error_code", length = 80)
    private String providerErrorCode;

    @Column(name = "provider_error_message")
    private String providerErrorMessage;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
