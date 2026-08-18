package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record FinancialStatementResponse(
        UUID companyId,
        LocalDate fromDate,
        LocalDate toDate,
        String statementType,
        List<FinancialStatementGroupResponse> groups,
        BigDecimal total) {
}
