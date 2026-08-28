package com.msvanegasg.facturaelectronica.reporting.interfaces.rest.dto;

import java.time.LocalDate;
import java.util.Map;

import com.msvanegasg.facturaelectronica.reporting.domain.model.ChartType;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportFormat;

import jakarta.validation.constraints.NotBlank;

public record ReportExportJobRequest(
        @NotBlank String reportCode,
        ReportExportFormat format,
        ChartType chartType,
        LocalDate from,
        LocalDate to,
        Map<String, String> filters,
        boolean notifyByEmail) {
}
