package com.msvanegasg.facturaelectronica.reporting.application.usecase;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportExportResult;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportQueryResult;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportFormat;

final class ReportTabularExporter {

    private ReportTabularExporter() {
    }

    static ReportExportResult export(ReportQueryResult result, ReportExportFormat format) {
        Table table = tableFrom(result.data());
        String content = switch (format) {
            case CSV -> csv(table);
            case XLS -> spreadsheetXml(table, result.reportCode());
        };
        String filename = "nexofiscal-" + result.reportCode().toLowerCase() + "-" + LocalDate.now() + "."
                + format.extension();
        return new ReportExportResult(filename, format.contentType(), content.getBytes(StandardCharsets.UTF_8), format);
    }

    private static Table tableFrom(JsonNode payload) {
        JsonNode source = selectTabularSource(payload);
        if (source == null || !source.isArray() || source.size() == 0) {
            Map<String, String> row = flatten(payload, "");
            return new Table(new ArrayList<>(row.keySet()), row.isEmpty() ? List.of() : List.of(row));
        }
        List<Map<String, String>> rows = new ArrayList<>();
        Set<String> columns = new LinkedHashSet<>();
        source.forEach(item -> {
            Map<String, String> row = flatten(item, "");
            rows.add(row);
            columns.addAll(row.keySet());
        });
        return new Table(new ArrayList<>(columns), rows);
    }

    private static JsonNode selectTabularSource(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return null;
        }
        if (payload.isArray()) {
            return payload;
        }
        if (!payload.isObject()) {
            return null;
        }
        JsonNode selected = null;
        for (Map.Entry<String, JsonNode> entry : payload.properties()) {
            if (entry.getValue().isArray() && (selected == null || entry.getValue().size() > selected.size())) {
                selected = entry.getValue();
            }
        }
        return selected;
    }

    private static Map<String, String> flatten(JsonNode node, String prefix) {
        Map<String, String> values = new LinkedHashMap<>();
        if (node == null || node.isNull()) {
            return values;
        }
        if (!node.isObject()) {
            values.put(prefix.isBlank() ? "value" : prefix, cellValue(node));
            return values;
        }
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            String key = prefix.isBlank() ? entry.getKey() : prefix + "." + entry.getKey();
            JsonNode value = entry.getValue();
            if (value.isObject()) {
                values.putAll(flatten(value, key));
            } else {
                values.put(key, cellValue(value));
            }
        }
        return values;
    }

    private static String csv(Table table) {
        StringBuilder builder = new StringBuilder("\uFEFF");
        builder.append(row(table.columns()));
        for (Map<String, String> item : table.rows()) {
            builder.append(row(table.columns().stream().map(column -> item.getOrDefault(column, "")).toList()));
        }
        return builder.toString();
    }

    private static String row(List<String> values) {
        return values.stream().map(ReportTabularExporter::csvCell).reduce((left, right) -> left + "," + right)
                .orElse("") + "\n";
    }

    private static String csvCell(String value) {
        String safeValue = value == null ? "" : value;
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }

    private static String spreadsheetXml(Table table, String reportCode) {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        builder.append("<?mso-application progid=\"Excel.Sheet\"?>");
        builder.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" ");
        builder.append("xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"><Worksheet ss:Name=\"");
        builder.append(xml(reportCode));
        builder.append("\"><Table>");
        builder.append(excelRow(table.columns()));
        for (Map<String, String> item : table.rows()) {
            builder.append(excelRow(table.columns().stream().map(column -> item.getOrDefault(column, "")).toList()));
        }
        builder.append("</Table></Worksheet></Workbook>");
        return builder.toString();
    }

    private static String excelRow(List<String> values) {
        StringBuilder builder = new StringBuilder("<Row>");
        values.forEach(value -> builder.append("<Cell><Data ss:Type=\"String\">").append(xml(value))
                .append("</Data></Cell>"));
        builder.append("</Row>");
        return builder.toString();
    }

    private static String xml(String value) {
        String safeValue = value == null ? "" : value;
        return safeValue.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static String cellValue(JsonNode value) {
        if (value == null || value.isNull()) {
            return "";
        }
        if (value.isValueNode()) {
            return value.asText();
        }
        if (value.isArray()) {
            return String.valueOf(value.size());
        }
        return value.toString();
    }

    private record Table(List<String> columns, List<Map<String, String>> rows) {
    }
}
