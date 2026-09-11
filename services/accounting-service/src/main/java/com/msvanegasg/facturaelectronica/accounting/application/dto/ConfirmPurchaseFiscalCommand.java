package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ConfirmPurchaseFiscalCommand(
        UUID companyId,
        UUID sourceId,
        UUID supplierId,
        LocalDate operationDate,
        String municipalityCode,
        boolean creditPurchase,
        LocalDate dueDate,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal total,
        UUID contractId,
        List<FiscalDocumentLineCommand> lines) {

    public ConfirmPurchaseFiscalCommand {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }
}
