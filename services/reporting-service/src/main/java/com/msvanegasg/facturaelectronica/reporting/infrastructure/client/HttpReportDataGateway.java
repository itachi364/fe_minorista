package com.msvanegasg.facturaelectronica.reporting.infrastructure.client;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportOption;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportDataGateway;
import com.msvanegasg.facturaelectronica.reporting.infrastructure.config.ReportingProperties;

public class HttpReportDataGateway implements ReportDataGateway {

    private final RestClient identityClient;
    private final RestClient inventoryClient;
    private final RestClient billingClient;
    private final RestClient accountingClient;
    private final RestClient payrollClient;
    private final RestClient tenantClient;
    private final ObjectMapper objectMapper;

    public HttpReportDataGateway(RestClient.Builder builder, ReportingProperties properties, ObjectMapper objectMapper) {
        this.identityClient = builder.clone().baseUrl(properties.identityUrl()).build();
        this.inventoryClient = builder.clone().baseUrl(properties.inventoryUrl()).build();
        this.billingClient = builder.clone().baseUrl(properties.billingUrl()).build();
        this.accountingClient = builder.clone().baseUrl(properties.accountingUrl()).build();
        this.payrollClient = builder.clone().baseUrl(properties.payrollUrl()).build();
        this.tenantClient = builder.clone().baseUrl(properties.tenantUrl()).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode fetchReport(UUID companyId, String reportCode, LocalDate from, LocalDate to,
            Map<String, String> filters, String authorizationHeader) {
        return switch (reportCode) {
            case "SALES_BY_SELLER", "SALES_BY_PRODUCT" -> get(billingClient, "/api/v1/reports/sales", companyId,
                    authorizationHeader, filters);
            case "PURCHASES" -> get(inventoryClient, "/api/v1/reports/purchases", companyId, authorizationHeader,
                    filters);
            case "INVENTORY_STOCK" -> get(inventoryClient, "/api/v1/reports/inventory-stock", companyId,
                    authorizationHeader, filters);
            case "PROFITABILITY", "DAILY_PROFIT_AND_LOSS" -> get(accountingClient,
                    "/api/v1/reports/income-statement", companyId, authorizationHeader, filters);
            case "ACCOUNTS_RECEIVABLE" -> get(accountingClient, "/api/v1/reports/accounts-receivable", companyId,
                    authorizationHeader, filters);
            case "PAYROLL_DAILY_PAYMENTS" -> get(payrollClient, "/api/v1/payroll/daily-payments", companyId,
                    authorizationHeader, filters);
            case "LICENSE_USAGE" -> get(tenantClient, "/api/v1/companies/" + companyId + "/license", companyId,
                    authorizationHeader, filters);
            default -> objectMapper.createObjectNode();
        };
    }

    @Override
    public List<ReportOption> fetchOptions(UUID companyId, String optionSource, String authorizationHeader) {
        return switch (optionSource) {
            case "SELLERS" -> users(companyId, authorizationHeader);
            case "PRODUCTS" -> products(companyId, authorizationHeader);
            case "ACTIVE_STATUS" -> List.of(new ReportOption("true", "Activo"), new ReportOption("false", "Inactivo"));
            case "ACCOUNT_STATUS" -> List.of(new ReportOption("PENDING", "Pendiente"),
                    new ReportOption("PAID", "Pagado"), new ReportOption("OVERDUE", "Vencido"));
            default -> List.of();
        };
    }

    private List<ReportOption> users(UUID companyId, String authorizationHeader) {
        JsonNode payload = get(identityClient, "/api/v1/companies/" + companyId + "/users", companyId,
                authorizationHeader, Map.of("active", "true"));
        if (!payload.isArray()) {
            return List.of();
        }
        return StreamSupport.stream(payload.spliterator(), false)
                .filter(item -> item.hasNonNull("id"))
                .filter(item -> hasSalesPermission(companyId, item.get("id").asText(), authorizationHeader))
                .map(item -> new ReportOption(item.get("id").asText(),
                        item.hasNonNull("email") ? item.get("email").asText() : item.get("id").asText()))
                .toList();
    }

    private List<ReportOption> products(UUID companyId, String authorizationHeader) {
        JsonNode payload = get(inventoryClient, "/api/v1/products", companyId, authorizationHeader,
                Map.of("active", "true"));
        if (!payload.isArray()) {
            return List.of();
        }
        return streamOptions(payload, "id", "name");
    }

    private JsonNode get(RestClient client, String path, UUID companyId, String authorizationHeader,
            Map<String, String> filters) {
        return client.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(path);
                    filters.forEach((key, value) -> {
                        if (value != null && !value.isBlank()) {
                            builder.queryParam(key, value);
                        }
                    });
                    return builder.build();
                })
                .headers(headers -> applyHeaders(headers, companyId, authorizationHeader))
                .retrieve()
                .body(JsonNode.class);
    }

    private static void applyHeaders(HttpHeaders headers, UUID companyId, String authorizationHeader) {
        headers.set("X-Company-Id", companyId.toString());
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
    }

    private static List<ReportOption> streamOptions(JsonNode payload, String valueField, String labelField) {
        return StreamSupport.stream(payload.spliterator(), false)
                .filter(item -> item.hasNonNull(valueField))
                .map(item -> new ReportOption(item.get(valueField).asText(),
                        item.hasNonNull(labelField) ? item.get(labelField).asText() : item.get(valueField).asText()))
                .toList();
    }

    private boolean hasSalesPermission(UUID companyId, String userId, String authorizationHeader) {
        JsonNode payload = get(identityClient,
                "/api/v1/companies/" + companyId + "/users/" + userId + "/effective-permissions", companyId,
                authorizationHeader, Map.of());
        JsonNode permissions = payload.get("permissions");
        if (permissions == null || !permissions.isArray()) {
            return false;
        }
        return StreamSupport.stream(permissions.spliterator(), false)
                .anyMatch(permission -> "SALES_CREATE".equals(permission.asText()));
    }
}
