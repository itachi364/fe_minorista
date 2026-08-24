package com.msvanegasg.facturaelectronica.reporting.domain.model;

import java.util.List;

public record ReportDefinition(
        String code,
        String label,
        String description,
        String category,
        List<ReportFilter> filters,
        List<ChartType> chartTypes) {
}
