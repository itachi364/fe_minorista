package com.msvanegasg.facturaelectronica.reporting.application.dto;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.reporting.domain.model.ChartType;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportFormat;

public record CreateReportExportJobCommand(
        UUID companyId,
        UUID requestedByUserId,
        String reportCode,
        ReportExportFormat format,
        ChartType chartType,
        LocalDate from,
        LocalDate to,
        Map<String, String> filters,
        boolean notifyByEmail,
        String authorizationHeader) {
}
