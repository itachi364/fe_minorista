package com.msvanegasg.facturaelectronica.bff.infrastructure.client;

import java.io.IOException;
import java.net.URI;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyRequest;
import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyResponse;
import com.msvanegasg.facturaelectronica.bff.application.port.out.InternalServiceGateway;
import com.msvanegasg.facturaelectronica.bff.domain.model.TargetService;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffProperties;

@Component
public class RestClientInternalServiceGateway implements InternalServiceGateway {

    private static final Set<String> FORWARDED_HEADERS = Set.of("authorization", "x-company-id", "x-correlation-id",
            "x-user-id", "idempotency-key", "content-type", "accept");
    private static final Set<String> RESPONSE_HEADERS = Set.of("content-type", "content-disposition",
            "x-correlation-id");
    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final Map<TargetService, AccessRule> ACCESS_RULES = Map.of(
            TargetService.CATALOG, new AccessRule(Set.of("COMPANY_CATALOGS_MANAGE", "COMPANY_SETTINGS_MANAGE"),
                    Set.of("COMPANY_CATALOGS_MANAGE", "COMPANY_SETTINGS_MANAGE")),
            TargetService.ACCOUNTING, new AccessRule(Set.of("ACCOUNTING_VIEW", "ACCOUNTING_MANAGE", "REPORTS_VIEW"),
                    Set.of("ACCOUNTING_MANAGE")),
            TargetService.INVENTORY, new AccessRule(Set.of("INVENTORY_VIEW", "INVENTORY_MANAGE", "SALES_CREATE"),
                    Set.of("INVENTORY_MANAGE")),
            TargetService.PAYROLL, new AccessRule(Set.of("PAYROLL_VIEW", "PAYROLL_MANAGE"), Set.of("PAYROLL_MANAGE")),
            TargetService.REPORTING, new AccessRule(Set.of("REPORTS_VIEW"), Set.of("REPORTS_VIEW")),
            TargetService.DIAN_PROVIDER, new AccessRule(Set.of("COMPANY_SETTINGS_MANAGE", "FISCAL_DOCUMENTS_ISSUE"),
                    Set.of("COMPANY_SETTINGS_MANAGE")),
            TargetService.AUDIT, new AccessRule(Set.of("AUDIT_VIEW", "GLOBAL_AUDIT_VIEW"), Set.of("AUDIT_VIEW",
                    "GLOBAL_AUDIT_VIEW")));

    private final Map<TargetService, RestClient> clients;
    private final ObjectMapper objectMapper;

    public RestClientInternalServiceGateway(RestClient.Builder builder, BffProperties properties,
            ObjectMapper objectMapper) {
        this.clients = new EnumMap<>(TargetService.class);
        this.clients.put(TargetService.TENANT, builder.clone().baseUrl(properties.tenantUrl()).build());
        this.clients.put(TargetService.IDENTITY, builder.clone().baseUrl(properties.identityUrl()).build());
        this.clients.put(TargetService.CATALOG, builder.clone().baseUrl(properties.catalogUrl()).build());
        this.clients.put(TargetService.THIRDPARTY, builder.clone().baseUrl(properties.thirdpartyUrl()).build());
        this.clients.put(TargetService.INVENTORY, builder.clone().baseUrl(properties.inventoryUrl()).build());
        this.clients.put(TargetService.BILLING, builder.clone().baseUrl(properties.billingUrl()).build());
        this.clients.put(TargetService.ACCOUNTING, builder.clone().baseUrl(properties.accountingUrl()).build());
        this.clients.put(TargetService.PAYROLL, builder.clone().baseUrl(properties.payrollUrl()).build());
        this.clients.put(TargetService.REPORTING, builder.clone().baseUrl(properties.reportingUrl()).build());
        this.clients.put(TargetService.DIAN_PROVIDER, builder.clone().baseUrl(properties.dianProviderUrl()).build());
        this.clients.put(TargetService.AUDIT, builder.clone().baseUrl(properties.auditUrl()).build());
        this.objectMapper = objectMapper;
    }

    @Override
    public ProxyResponse exchange(ProxyRequest request) {
        RestClient client = clients.get(request.targetService());
        if (client == null) {
            throw new DownstreamServiceException("Servicio interno no configurado: " + request.targetService(), null);
        }
        authorize(request);
        try {
            ProxyResponse response = client.method(request.method())
                    .uri(request.uri())
                    .headers(headers -> copyRequestHeaders(request.headers(), headers))
                    .body(request.body() == null ? new byte[0] : request.body())
                    .exchange((clientRequest, clientResponse) -> {
                        byte[] responseBody = clientResponse.getBody().readAllBytes();
                        HttpHeaders responseHeaders = filterResponseHeaders(clientResponse.getHeaders());
                        return new ProxyResponse(clientResponse.getStatusCode(), responseHeaders, responseBody);
                    });
            auditMutableRequest(request, response.status().isError() ? "FAILURE" : "SUCCESS",
                    "status=" + response.status().value(), response.body());
            return response;
        } catch (RestClientException exception) {
            auditMutableRequest(request, "FAILURE", "downstream_unavailable", null);
            throw new DownstreamServiceException("No fue posible comunicarse con el servicio interno.", exception);
        }
    }

    private void authorize(ProxyRequest request) {
        if (request.targetService() == TargetService.IDENTITY) {
            return;
        }
        if (request.targetService() == TargetService.TENANT && !MUTATING_METHODS.contains(request.method().name())) {
            return;
        }
        AccessRule rule = accessRuleFor(request);
        if (rule == null && request.targetService() != TargetService.TENANT) {
            return;
        }
        String authorization = request.headers().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            throw new BffAccessDeniedException("authorization is required");
        }
        if (isRoot(authorization)) {
            return;
        }
        if (request.targetService() == TargetService.TENANT && rule == null) {
            throw new BffAccessDeniedException("ROOT is required for platform administration");
        }
        UUID userId = currentUserId(authorization);
        UUID headerUserId = parseUuid(request.headers().getFirst("X-User-Id"));
        if (headerUserId == null || !headerUserId.equals(userId)) {
            throw new BffAccessDeniedException("X-User-Id does not match authenticated user");
        }
        UUID companyId = parseUuid(request.headers().getFirst("X-Company-Id"));
        if (companyId == null) {
            throw new BffAccessDeniedException("X-Company-Id is required");
        }
        Set<String> requiredPermissions = MUTATING_METHODS.contains(request.method().name()) ? rule.writePermissions()
                : rule.readPermissions();
        Set<String> actualPermissions = effectivePermissions(companyId, userId);
        if (actualPermissions.stream().noneMatch(requiredPermissions::contains)) {
            throw new BffAccessDeniedException("insufficient permissions");
        }
    }

    private static AccessRule accessRuleFor(ProxyRequest request) {
        if (request.targetService() == TargetService.BILLING) {
            return billingAccessRule(request.uri());
        }
        if (request.targetService() == TargetService.TENANT) {
            return tenantAccessRule(request.uri());
        }
        return ACCESS_RULES.get(request.targetService());
    }

    private static AccessRule tenantAccessRule(URI uri) {
        String normalized = normalizeApiPath(uri.getPath());
        if (normalized.matches("companies/[^/]+/branding(/.*)?")) {
            return new AccessRule(Set.of("COMPANY_SETTINGS_MANAGE"), Set.of("COMPANY_SETTINGS_MANAGE"));
        }
        return null;
    }

    private static AccessRule billingAccessRule(URI uri) {
        String normalized = normalizeApiPath(uri.getPath());
        if (matchesAny(normalized, "reports/sales", "reports/electronic-documents")) {
            return new AccessRule(Set.of("REPORTS_VIEW", "SALES_CREATE", "FISCAL_DOCUMENTS_ISSUE"), Set.of());
        }
        if (matchesAny(normalized, "sales")) {
            return new AccessRule(Set.of("SALES_CREATE", "REPORTS_VIEW", "FISCAL_DOCUMENTS_ISSUE"),
                    Set.of("SALES_CREATE"));
        }
        if (normalized.matches("electronic-pos/[^/]+/adjustment-notes(/.*)?")) {
            return fiscalDocumentAccessRule();
        }
        if (matchesAny(normalized, "electronic-pos")) {
            return new AccessRule(Set.of("SALES_CREATE", "REPORTS_VIEW", "FISCAL_DOCUMENTS_ISSUE"),
                    Set.of("SALES_CREATE", "FISCAL_DOCUMENTS_ISSUE"));
        }
        if (matchesAny(normalized, "electronic-invoices")) {
            return new AccessRule(Set.of("SALES_CREATE", "REPORTS_VIEW", "FISCAL_DOCUMENTS_ISSUE"),
                    Set.of("SALES_CREATE", "FISCAL_DOCUMENTS_ISSUE"));
        }
        if (matchesAny(normalized, "issuers", "numbering-resolutions")) {
            return new AccessRule(Set.of("FISCAL_DOCUMENTS_ISSUE", "COMPANY_SETTINGS_MANAGE"),
                    Set.of("FISCAL_DOCUMENTS_ISSUE", "COMPANY_SETTINGS_MANAGE"));
        }
        if (matchesAny(normalized, "credit-notes", "debit-notes")) {
            return fiscalDocumentAccessRule();
        }
        return new AccessRule(Set.of("FISCAL_DOCUMENTS_ISSUE"), Set.of("FISCAL_DOCUMENTS_ISSUE"));
    }

    private static AccessRule fiscalDocumentAccessRule() {
        return new AccessRule(Set.of("FISCAL_DOCUMENTS_ISSUE"), Set.of("FISCAL_DOCUMENTS_ISSUE"));
    }

    private boolean isRoot(String authorization) {
        try {
            clients.get(TargetService.IDENTITY)
                    .get()
                    .uri("/api/v1/platform/permissions")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private UUID currentUserId(String authorization) {
        try {
            UserResponse response = clients.get(TargetService.IDENTITY)
                    .get()
                    .uri("/api/v1/me")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .body(UserResponse.class);
            if (response == null || response.id() == null) {
                throw new BffAccessDeniedException("authenticated user is required");
            }
            return response.id();
        } catch (BffAccessDeniedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BffAccessDeniedException("invalid authorization");
        }
    }

    private Set<String> effectivePermissions(UUID companyId, UUID userId) {
        try {
            CompanyAccessResponse response = clients.get(TargetService.IDENTITY)
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/companies/{companyId}/permissions")
                            .queryParam("userId", userId)
                            .build(companyId))
                    .retrieve()
                    .body(CompanyAccessResponse.class);
            if (response == null || response.permissions() == null) {
                return Set.of();
            }
            return Set.copyOf(response.permissions());
        } catch (RuntimeException exception) {
            throw new BffAccessDeniedException("permissions could not be resolved");
        }
    }

    private static void copyRequestHeaders(HttpHeaders source, HttpHeaders target) {
        source.forEach((name, values) -> {
            if (FORWARDED_HEADERS.contains(name.toLowerCase())) {
                target.put(name, values);
            }
        });
    }

    private static HttpHeaders filterResponseHeaders(HttpHeaders source) {
        HttpHeaders target = new HttpHeaders();
        source.forEach((name, values) -> {
            if (RESPONSE_HEADERS.contains(name.toLowerCase())) {
                target.put(name, values);
            }
        });
        return target;
    }

    private void auditMutableRequest(ProxyRequest request, String result, String detail, byte[] responseBody) {
        if (request.targetService() == TargetService.AUDIT || !MUTATING_METHODS.contains(request.method().name())) {
            return;
        }
        UUID companyId = resolveAuditCompanyId(request, responseBody);
        if (companyId == null) {
            return;
        }
        try {
            clients.get(TargetService.AUDIT)
                    .post()
                    .uri("/api/v1/audit-events")
                    .header("X-Company-Id", companyId.toString())
                    .body(new AuditRequest(parseUuid(request.headers().getFirst("X-User-Id")),
                            "BFF_MUTATION", request.targetService().name(), truncate(request.uri().getPath(), 120),
                            request.method().name(), result, auditDetail(request, detail)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException ignored) {
            // La auditoria es best-effort en modo sincrono para no tumbar la operacion principal.
        }
    }

    private UUID resolveAuditCompanyId(ProxyRequest request, byte[] responseBody) {
        UUID headerCompanyId = parseUuid(request.headers().getFirst("X-Company-Id"));
        if (headerCompanyId != null) {
            return headerCompanyId;
        }
        if (request.targetService() != TargetService.TENANT
                || !"POST".equals(request.method().name())
                || !"/api/v1/companies".equals(request.uri().getPath())
                || responseBody == null
                || responseBody.length == 0) {
            return null;
        }
        try {
            JsonNode idNode = objectMapper.readTree(responseBody).path("id");
            return parseUuid(idNode.isMissingNode() ? null : idNode.asText());
        } catch (RuntimeException ignored) {
            return null;
        } catch (IOException ignored) {
            return null;
        }
    }

    private static UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static String normalizeApiPath(String path) {
        String value = path == null ? "" : path.strip().toLowerCase(Locale.ROOT);
        if (value.startsWith("/")) {
            value = value.substring(1);
        }
        if (value.startsWith("api/v1/")) {
            value = value.substring("api/v1/".length());
        }
        return value;
    }

    private static boolean matchesAny(String normalized, String... prefixes) {
        for (String prefix : prefixes) {
            if (normalized.equals(prefix) || normalized.startsWith(prefix + "/")) {
                return true;
            }
        }
        return false;
    }

    private static String auditDetail(ProxyRequest request, String detail) {
        return "{\"method\":\"%s\",\"path\":\"%s\",\"correlationId\":\"%s\",\"detail\":\"%s\"}"
                .formatted(escape(request.method().name()), escape(request.uri().getPath()),
                        escape(request.headers().getFirst("X-Correlation-Id")), escape(detail));
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private record AuditRequest(UUID userId, String eventType, String resourceType, String resourceId, String action,
            String result, String detail) {
    }

    private record AccessRule(Set<String> readPermissions, Set<String> writePermissions) {
    }

    private record UserResponse(UUID id) {
    }

    private record CompanyAccessResponse(UUID companyId, List<String> roles, Set<String> permissions) {
    }
}
