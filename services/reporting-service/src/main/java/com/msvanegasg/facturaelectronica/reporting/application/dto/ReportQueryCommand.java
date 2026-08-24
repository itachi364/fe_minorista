package com.msvanegasg.facturaelectronica.reporting.application.dto;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.reporting.domain.model.ChartType;

public record ReportQueryCommand(
        UUID companyId,
        String reportCode,
        LocalDate from,
        LocalDate to,
        Map<String, String> filters,
        ChartType chartType,
        String authorizationHeader) {
}
