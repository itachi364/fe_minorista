package com.msvanegasg.facturaelectronica.reporting.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportOption;

public interface ReportDataGateway {

    JsonNode fetchReport(UUID companyId, String reportCode, LocalDate from, LocalDate to, Map<String, String> filters,
            String authorizationHeader);

    List<ReportOption> fetchOptions(UUID companyId, String optionSource, String authorizationHeader);
}
