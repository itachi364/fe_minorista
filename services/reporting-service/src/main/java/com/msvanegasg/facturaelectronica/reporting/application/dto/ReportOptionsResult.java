package com.msvanegasg.facturaelectronica.reporting.application.dto;

import java.util.List;
import java.util.Map;

public record ReportOptionsResult(String reportCode, Map<String, List<ReportOption>> options) {
}
