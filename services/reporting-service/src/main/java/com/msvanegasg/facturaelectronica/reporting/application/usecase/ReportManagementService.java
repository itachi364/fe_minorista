package com.msvanegasg.facturaelectronica.reporting.application.usecase;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportOption;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportExportResult;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportOptionsResult;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportQueryCommand;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportQueryResult;
import com.msvanegasg.facturaelectronica.reporting.application.port.in.ManageReportsUseCase;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportDataGateway;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ChartType;
import com.msvanegasg.facturaelectronica.reporting.domain.model.FilterType;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportCatalog;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportDefinition;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportFormat;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportFilter;

public class ReportManagementService implements ManageReportsUseCase {

    private final ReportDataGateway dataGateway;

    public ReportManagementService(ReportDataGateway dataGateway) {
        this.dataGateway = Objects.requireNonNull(dataGateway);
    }

    @Override
    public List<ReportDefinition> definitions() {
        return ReportCatalog.definitions();
    }

    @Override
    public ReportOptionsResult options(UUID companyId, String reportCode, String authorizationHeader) {
        requireCompany(companyId);
        ReportDefinition definition = findDefinition(reportCode);
        Map<String, List<ReportOption>> options = new HashMap<>();
        for (ReportFilter filter : definition.filters()) {
            if (filter.type() == FilterType.SELECT && filter.optionSource() != null) {
                options.put(filter.code(), dataGateway.fetchOptions(companyId, filter.optionSource(), authorizationHeader));
            }
        }
        return new ReportOptionsResult(definition.code(), options);
    }

    @Override
    public ReportQueryResult query(ReportQueryCommand command) {
        requireCompany(command.companyId());
        ReportDefinition definition = findDefinition(command.reportCode());
        validateRequiredFilters(definition, command);
        ChartType chartType = command.chartType() == null ? ChartType.TABLE : command.chartType();
        if (!definition.chartTypes().contains(chartType)) {
            throw new IllegalArgumentException("chartType is not allowed for report " + definition.code());
        }
        Map<String, String> filters = normalizedFilters(command);
        return new ReportQueryResult(command.companyId(), definition.code(), chartType, filters,
                dataGateway.fetchReport(command.companyId(), definition.code(), command.from(), command.to(), filters,
                        command.authorizationHeader()),
                Instant.now());
    }

    @Override
    public ReportExportResult export(ReportQueryCommand command, ReportExportFormat format) {
        ReportExportFormat safeFormat = format == null ? ReportExportFormat.CSV : format;
        return ReportTabularExporter.export(query(command), safeFormat);
    }

    private static ReportDefinition findDefinition(String reportCode) {
        if (reportCode == null || reportCode.isBlank()) {
            throw new IllegalArgumentException("reportCode is required");
        }
        return ReportCatalog.find(reportCode).orElseThrow(() -> new ReportNotFoundException(reportCode));
    }

    private static void validateRequiredFilters(ReportDefinition definition, ReportQueryCommand command) {
        for (ReportFilter filter : definition.filters()) {
            if (!filter.required()) {
                continue;
            }
            if ("from".equals(filter.code()) && command.from() == null) {
                throw new IllegalArgumentException("from is required");
            }
            if ("to".equals(filter.code()) && command.to() == null) {
                throw new IllegalArgumentException("to is required");
            }
        }
        if (command.from() != null && command.to() != null && command.to().isBefore(command.from())) {
            throw new IllegalArgumentException("to must be greater than or equal to from");
        }
    }

    private static Map<String, String> normalizedFilters(ReportQueryCommand command) {
        Map<String, String> filters = new HashMap<>();
        if (command.filters() != null) {
            command.filters().forEach((key, value) -> {
                if (key != null && value != null && !value.isBlank()) {
                    filters.put(key, value);
                }
            });
        }
        if (command.from() != null) {
            filters.put("from", command.from().toString());
        }
        if (command.to() != null) {
            filters.put("to", command.to().toString());
        }
        return Map.copyOf(filters);
    }

    private static void requireCompany(UUID companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required");
        }
    }
}
