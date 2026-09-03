package com.msvanegasg.facturaelectronica.bff.interfaces.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffProperties;

@RestController
@RequestMapping("/api/v1/readiness")
public class CompanyReadinessController {

    private static final String COMPANY_HEADER = "X-Company-Id";

    private final RestClient tenantClient;
    private final RestClient billingClient;
    private final RestClient accountingClient;
    private final RestClient inventoryClient;

    public CompanyReadinessController(RestClient.Builder builder, BffProperties properties) {
        this.tenantClient = builder.clone().baseUrl(properties.tenantUrl()).build();
        this.billingClient = builder.clone().baseUrl(properties.billingUrl()).build();
        this.accountingClient = builder.clone().baseUrl(properties.accountingUrl()).build();
        this.inventoryClient = builder.clone().baseUrl(properties.inventoryUrl()).build();
    }

    @GetMapping("/company")
    public ResponseEntity<CompanyReadinessResponse> companyReadiness(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        List<ReadinessItemResponse> items = new ArrayList<>();
        items.add(checkLicense(companyId, authorizationHeader));
        items.add(checkIssuer(companyId, authorizationHeader));
        items.add(checkNumberingResolution(companyId, authorizationHeader));
        items.add(checkFiscalPolicy(companyId, authorizationHeader));
        items.add(checkAccountingAccounts(companyId, authorizationHeader));
        items.add(checkAccountingRules(companyId, authorizationHeader));
        items.add(checkInventory(companyId, authorizationHeader));
        return ResponseEntity.ok(new CompanyReadinessResponse(companyId, status(items), items));
    }

    private ReadinessItemResponse checkLicense(UUID companyId, String authorizationHeader) {
        JsonNode payload = get(tenantClient, "/api/v1/companies/" + companyId + "/license", companyId,
                authorizationHeader, Map.of());
        String status = text(payload, "status").toUpperCase(Locale.ROOT);
        boolean valid = "ACTIVE".equals(status);
        return item("LICENSE", "Licencia empresarial", valid ? "READY" : "BLOCKED",
                valid ? "La licencia esta activa." : "La empresa necesita una licencia activa.",
                "Licencias");
    }

    private ReadinessItemResponse checkIssuer(UUID companyId, String authorizationHeader) {
        JsonNode payload = get(billingClient, "/api/v1/issuers", companyId, authorizationHeader, Map.of());
        boolean ready = containsActive(payload);
        return item("FISCAL_ISSUER", "Emisor fiscal", ready ? "READY" : "BLOCKED",
                ready ? "Existe un emisor fiscal activo." : "Configura al menos un emisor fiscal activo.",
                "Fiscal");
    }

    private ReadinessItemResponse checkNumberingResolution(UUID companyId, String authorizationHeader) {
        JsonNode payload = get(billingClient, "/api/v1/numbering-resolutions", companyId, authorizationHeader,
                Map.of());
        boolean ready = containsActive(payload);
        return item("NUMBERING_RESOLUTION", "Resolucion fiscal", ready ? "READY" : "BLOCKED",
                ready ? "Existe una resolucion activa." : "Configura una resolucion activa para poder cerrar ventas.",
                "Fiscal");
    }

    private ReadinessItemResponse checkFiscalPolicy(UUID companyId, String authorizationHeader) {
        JsonNode payload = get(billingClient, "/api/v1/fiscal-policy", companyId, authorizationHeader, Map.of());
        boolean ready = payload != null && !payload.isMissingNode() && payload.hasNonNull("defaultSaleDocumentType");
        return item("FISCAL_POLICY", "Politica fiscal de ventas", ready ? "READY" : "WARNING",
                ready ? "La empresa tiene documento fiscal por defecto." :
                        "Define el documento fiscal por defecto para ventas.",
                "Fiscal");
    }

    private ReadinessItemResponse checkAccountingAccounts(UUID companyId, String authorizationHeader) {
        JsonNode payload = get(accountingClient, "/api/v1/accounts", companyId, authorizationHeader,
                Map.of("active", "true"));
        boolean ready = payload != null && payload.isArray() && payload.size() > 0;
        return item("ACCOUNTING_ACCOUNTS", "Plan de cuentas", ready ? "READY" : "BLOCKED",
                ready ? "El plan de cuentas tiene cuentas activas." :
                        "Crea o completa la plantilla basica del plan de cuentas.",
                "Configuracion contable");
    }

    private ReadinessItemResponse checkAccountingRules(UUID companyId, String authorizationHeader) {
        JsonNode payload = get(accountingClient, "/api/v1/accounting-rules", companyId, authorizationHeader,
                Map.of("active", "true"));
        boolean ready = payload != null && payload.isArray() && payload.size() > 0;
        return item("ACCOUNTING_RULES", "Reglas contables", ready ? "READY" : "BLOCKED",
                ready ? "Existen reglas contables activas." :
                        "Crea reglas contables activas antes de cerrar operaciones.",
                "Configuracion contable");
    }

    private ReadinessItemResponse checkInventory(UUID companyId, String authorizationHeader) {
        JsonNode payload = get(inventoryClient, "/api/v1/products", companyId, authorizationHeader,
                Map.of("active", "true"));
        boolean ready = payload != null && payload.isArray() && payload.size() > 0;
        return item("INVENTORY", "Inventario operativo", ready ? "READY" : "WARNING",
                ready ? "Hay productos o servicios activos." :
                        "Registra productos o servicios antes de operar ventas recurrentes.",
                "Inventario");
    }

    private JsonNode get(RestClient client, String path, UUID companyId, String authorizationHeader,
            Map<String, String> queryParams) {
        try {
            return client.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path(path);
                        queryParams.forEach(builder::queryParam);
                        return builder.build();
                    })
                    .headers(headers -> {
                        headers.set(COMPANY_HEADER, companyId.toString());
                        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
                        }
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new IllegalStateException("readiness downstream " + URI.create(path) + " failed");
                    })
                    .body(JsonNode.class);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static ReadinessItemResponse item(String code, String label, String status, String message,
            String actionStep) {
        return new ReadinessItemResponse(code, label, status, message, actionStep);
    }

    private static String status(List<ReadinessItemResponse> items) {
        if (items.stream().anyMatch(item -> "BLOCKED".equals(item.status()))) {
            return "BLOCKED";
        }
        if (items.stream().anyMatch(item -> "WARNING".equals(item.status()))) {
            return "WARNING";
        }
        return "READY";
    }

    private static boolean containsActive(JsonNode payload) {
        if (payload == null || !payload.isArray()) {
            return false;
        }
        for (JsonNode item : payload) {
            if (item.path("active").asBoolean(false)) {
                return true;
            }
        }
        return false;
    }

    private static String text(JsonNode payload, String field) {
        if (payload == null || !payload.hasNonNull(field)) {
            return "";
        }
        return payload.get(field).asText("");
    }

    public record CompanyReadinessResponse(UUID companyId, String status, List<ReadinessItemResponse> items) {
    }

    public record ReadinessItemResponse(String code, String label, String status, String message, String actionStep) {
    }
}
