package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import java.util.UUID;

public record Sale(UUID id, UUID companyId, BuyerIdentificationMode buyerIdentificationMode, UUID customerId,
        PaymentMethodCode paymentMethodCode,
        VirtualWalletCode virtualWalletCode, SaleChannel saleChannel, SaleStatus status, BigDecimal subtotal,
        BigDecimal discountTotal, BigDecimal taxTotal, BigDecimal total, String idempotencyKey, UUID createdBy,
        Instant createdAt, Instant confirmedAt, List<SaleLine> lines, ElectronicDocument electronicDocument) {

    public Sale {
        require(id, "id");
        require(companyId, "companyId");
        buyerIdentificationMode = buyerIdentificationMode == null ? BuyerIdentificationMode.IDENTIFIED_CUSTOMER : buyerIdentificationMode;
        require(paymentMethodCode, "paymentMethodCode");
        require(saleChannel, "saleChannel");
        require(status, "status");
        require(subtotal, "subtotal");
        require(discountTotal, "discountTotal");
        require(taxTotal, "taxTotal");
        require(total, "total");
        require(idempotencyKey, "idempotencyKey");
        require(createdAt, "createdAt");
        lines = List.copyOf(Objects.requireNonNull(lines, "lines are required"));
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("lines are required");
        }
        if (paymentMethodCode == PaymentMethodCode.VIRTUAL_WALLET && virtualWalletCode == null) {
            throw new IllegalArgumentException("virtualWalletCode is required for VIRTUAL_WALLET payment method");
        }
        if (paymentMethodCode != PaymentMethodCode.VIRTUAL_WALLET && virtualWalletCode != null) {
            throw new IllegalArgumentException("virtualWalletCode only applies to VIRTUAL_WALLET payment method");
        }
    }

    public static Sale draft(UUID id, UUID companyId, BuyerIdentificationMode buyerIdentificationMode, UUID customerId,
            PaymentMethodCode paymentMethodCode,
            VirtualWalletCode virtualWalletCode, SaleChannel saleChannel, String idempotencyKey, UUID createdBy,
            Instant createdAt, List<SaleLine> lines) {
        BigDecimal subtotal = lines.stream().map(SaleLine::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = lines.stream().map(SaleLine::discountAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = lines.stream().map(SaleLine::taxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = lines.stream().map(SaleLine::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Sale(id, companyId, buyerIdentificationMode, customerId, paymentMethodCode, virtualWalletCode,
                saleChannel, SaleStatus.DRAFT, subtotal, discount, tax, total, idempotencyKey, createdBy, createdAt,
                null, lines, null);
    }

    public static Sale draft(UUID id, UUID companyId, UUID customerId, PaymentMethodCode paymentMethodCode,
            VirtualWalletCode virtualWalletCode, SaleChannel saleChannel, String idempotencyKey, UUID createdBy,
            Instant createdAt, List<SaleLine> lines) {
        BuyerIdentificationMode buyerMode = customerId == null ? BuyerIdentificationMode.FINAL_CONSUMER
                : BuyerIdentificationMode.IDENTIFIED_CUSTOMER;
        return draft(id, companyId, buyerMode, customerId, paymentMethodCode, virtualWalletCode, saleChannel,
                idempotencyKey, createdBy, createdAt, lines);
    }

    public Sale confirm(ElectronicDocument document, Instant confirmedAt) {
        require(document, "document");
        SaleStatus nextStatus = document.status() == ElectronicDocumentStatus.VALIDATED
                ? SaleStatus.CONFIRMED
                : SaleStatus.REJECTED;
        return new Sale(id, companyId, buyerIdentificationMode, customerId, paymentMethodCode, virtualWalletCode,
                saleChannel, nextStatus, subtotal, discountTotal, taxTotal, total, idempotencyKey, createdBy,
                createdAt, confirmedAt, lines, document);
    }

    public Sale withElectronicDocument(ElectronicDocument document) {
        require(document, "document");
        return new Sale(id, companyId, buyerIdentificationMode, customerId, paymentMethodCode, virtualWalletCode,
                saleChannel, status, subtotal, discountTotal, taxTotal, total, idempotencyKey, createdBy, createdAt,
                confirmedAt, lines, document);
    }

    private static void require(Object value, String field) {
        Objects.requireNonNull(value, field + " is required");
    }
}
