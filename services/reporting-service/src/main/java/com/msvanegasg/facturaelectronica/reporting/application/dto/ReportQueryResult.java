package com.msvanegasg.facturaelectronica.reporting.application.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ChartType;

public record ReportQueryResult(
        UUID companyId,
        String reportCode,
        ChartType chartType,
        Map<String, String> appliedFilters,
        JsonNode data,
        Instant generatedAt) {
}
