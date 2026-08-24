package com.msvanegasg.facturaelectronica.reporting.interfaces.rest.dto;

import java.time.LocalDate;
import java.util.Map;

import com.msvanegasg.facturaelectronica.reporting.domain.model.ChartType;

import jakarta.validation.constraints.NotBlank;

public record ReportQueryRequest(
        @NotBlank String reportCode,
        LocalDate from,
        LocalDate to,
        Map<String, String> filters,
        ChartType chartType) {
}
