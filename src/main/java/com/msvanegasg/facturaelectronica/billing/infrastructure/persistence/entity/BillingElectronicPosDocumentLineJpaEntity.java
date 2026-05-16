package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "billing_electronic_pos_document_line")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingElectronicPosDocumentLineJpaEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private BillingElectronicPosDocumentJpaEntity document;

    @Column(name = "product_id")
    private UUID productId;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;

    @Column(name = "tax_code", nullable = false, length = 50)
    private String taxCode;

    @Column(name = "tax_rate", nullable = false)
    private BigDecimal taxRate;

    @Column(name = "gross_amount", nullable = false)
    private BigDecimal grossAmount;

    @Column(name = "taxable_amount", nullable = false)
    private BigDecimal taxableAmount;

    @Column(name = "tax_amount", nullable = false)
    private BigDecimal taxAmount;

    @Column(name = "line_total", nullable = false)
    private BigDecimal lineTotal;
}
