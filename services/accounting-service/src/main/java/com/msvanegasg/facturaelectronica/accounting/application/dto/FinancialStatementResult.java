package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record FinancialStatementResult(
        UUID companyId,
        LocalDate fromDate,
        LocalDate toDate,
        String statementType,
        List<FinancialStatementGroupResult> groups,
        BigDecimal total) {
}
