package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FiscalReconciliationResult(UUID companyId, int year, int month, BigDecimal fiscalTotal,
        BigDecimal accountingTotal, BigDecimal difference, String status) {
}
