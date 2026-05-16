package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record DocumentLineToCalculate(
        UUID productId,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal discountAmount,
        String taxCode,
        BigDecimal taxRate) {

    public DocumentLineToCalculate {
        Objects.requireNonNull(productId, "productId is required");
        Objects.requireNonNull(quantity, "quantity is required");
        Objects.requireNonNull(unitPrice, "unitPrice is required");
        Objects.requireNonNull(discountAmount, "discountAmount is required");
        Objects.requireNonNull(taxRate, "taxRate is required");

        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unitPrice must be greater than or equal to zero");
        }
        if (discountAmount.signum() < 0) {
            throw new IllegalArgumentException("discountAmount must be greater than or equal to zero");
        }
        if (taxRate.signum() < 0) {
            throw new IllegalArgumentException("taxRate must be greater than or equal to zero");
        }
        if (taxCode == null || taxCode.isBlank()) {
            throw new IllegalArgumentException("taxCode is required");
        }

        taxCode = taxCode.trim();
    }
}
