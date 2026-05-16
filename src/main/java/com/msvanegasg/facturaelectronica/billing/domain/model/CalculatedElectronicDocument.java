package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record CalculatedElectronicDocument(
        List<CalculatedDocumentLine> lines,
        BigDecimal grossAmount,
        BigDecimal discountTotal,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal total) {

    public CalculatedElectronicDocument {
        lines = List.copyOf(lines);
    }
}
