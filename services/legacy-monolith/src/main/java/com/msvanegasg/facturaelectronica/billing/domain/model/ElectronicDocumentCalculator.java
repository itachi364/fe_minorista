package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class ElectronicDocumentCalculator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final int MONEY_SCALE = 2;

    private ElectronicDocumentCalculator() {
    }

    public static CalculatedElectronicDocument calculate(List<DocumentLineToCalculate> lines) {
        Objects.requireNonNull(lines, "lines are required");
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("at least one line is required");
        }

        List<CalculatedDocumentLine> calculatedLines = lines.stream()
                .map(ElectronicDocumentCalculator::calculateLine)
                .toList();

        BigDecimal grossAmount = sum(calculatedLines.stream()
                .map(CalculatedDocumentLine::grossAmount)
                .toList());
        BigDecimal discountTotal = sum(calculatedLines.stream()
                .map(CalculatedDocumentLine::discountAmount)
                .toList());
        BigDecimal subtotal = sum(calculatedLines.stream()
                .map(CalculatedDocumentLine::taxableAmount)
                .toList());
        BigDecimal taxTotal = sum(calculatedLines.stream()
                .map(CalculatedDocumentLine::taxAmount)
                .toList());
        BigDecimal total = sum(calculatedLines.stream()
                .map(CalculatedDocumentLine::lineTotal)
                .toList());

        return new CalculatedElectronicDocument(
                calculatedLines,
                grossAmount,
                discountTotal,
                subtotal,
                taxTotal,
                total);
    }

    private static CalculatedDocumentLine calculateLine(DocumentLineToCalculate line) {
        BigDecimal grossAmount = money(line.quantity().multiply(line.unitPrice()));
        BigDecimal discountAmount = money(line.discountAmount());

        if (discountAmount.compareTo(grossAmount) > 0) {
            throw new IllegalArgumentException("discountAmount must be less than or equal to grossAmount");
        }

        BigDecimal taxableAmount = money(grossAmount.subtract(discountAmount));
        BigDecimal taxAmount = money(taxableAmount.multiply(line.taxRate()).divide(ONE_HUNDRED));
        BigDecimal lineTotal = money(taxableAmount.add(taxAmount));

        return new CalculatedDocumentLine(
                line.productId(),
                line.quantity(),
                money(line.unitPrice()),
                discountAmount,
                line.taxCode(),
                line.taxRate(),
                grossAmount,
                taxableAmount,
                taxAmount,
                lineTotal);
    }

    private static BigDecimal sum(List<BigDecimal> values) {
        return money(values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
