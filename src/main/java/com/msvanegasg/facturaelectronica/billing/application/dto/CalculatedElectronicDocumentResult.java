package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record CalculatedElectronicDocumentResult(
        List<CalculatedElectronicDocumentLineResult> lines,
        BigDecimal grossAmount,
        BigDecimal discountTotal,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal total) {

    public CalculatedElectronicDocumentResult {
        lines = List.copyOf(lines);
    }
}
