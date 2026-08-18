package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;

public record FinancialStatementGroupResult(
        String code,
        String label,
        BigDecimal total) {
}
