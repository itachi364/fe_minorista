package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;

public record FinancialStatementGroupResponse(
        String code,
        String label,
        BigDecimal total) {
}
