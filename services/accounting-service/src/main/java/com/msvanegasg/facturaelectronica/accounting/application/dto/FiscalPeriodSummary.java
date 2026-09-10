package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record FiscalPeriodSummary(UUID companyId, int year, int month, Map<String, BigDecimal> bases,
        Map<String, BigDecimal> amounts, BigDecimal totalWithheld, boolean closed) {
}
