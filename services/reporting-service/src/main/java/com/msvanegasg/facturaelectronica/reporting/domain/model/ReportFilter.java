package com.msvanegasg.facturaelectronica.reporting.domain.model;

public record ReportFilter(
        String code,
        String label,
        FilterType type,
        boolean required,
        String optionSource) {
}
