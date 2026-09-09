package com.msvanegasg.facturaelectronica.bff.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyRequest;
import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyResponse;
import com.msvanegasg.facturaelectronica.bff.domain.model.TargetService;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffProperties;
import com.sun.net.httpserver.HttpServer;

class RestClientInternalServiceGatewayTest {

    private static final String COMPANY_ID = "11111111-1111-1111-1111-111111111111";
    private static final String USER_ID = "22222222-2222-2222-2222-222222222222";
    private static final String SALE_ID = "33333333-3333-3333-3333-333333333333";

    private HttpServer identityServer;
    private HttpServer tenantServer;
    private HttpServer payrollServer;
    private HttpServer billingServer;
    private HttpServer inventoryServer;
    private HttpServer auditServer;
    private HttpServer catalogServer;
    private HttpServer accountingServer;
    private HttpServer reportingServer;
    private HttpServer thirdpartyServer;

    @AfterEach
    void tearDown() {
        if (identityServer != null) {
            identityServer.stop(0);
        }
        if (tenantServer != null) {
            tenantServer.stop(0);
        }
        if (payrollServer != null) {
            payrollServer.stop(0);
        }
        if (billingServer != null) {
            billingServer.stop(0);
        }
        if (inventoryServer != null) {
            inventoryServer.stop(0);
        }
        if (auditServer != null) {
            auditServer.stop(0);
        }
        if (catalogServer != null) {
            catalogServer.stop(0);
        }
        if (accountingServer != null) {
            accountingServer.stop(0);
        }
        if (reportingServer != null) {
            reportingServer.stop(0);
        }
        if (thirdpartyServer != null) {
            thirdpartyServer.stop(0);
        }
    }

    @Test
    void allowsPayrollMutationWhenUserHasManagePermission() throws IOException {
        startIdentityServer("[\"PAYROLL_MANAGE\"]");
        startLicenseServer(true);
        CapturingHandler payrollHandler = startPayrollServer();
        RestClientInternalServiceGateway gateway = gateway();

        ProxyResponse response = gateway.exchange(new ProxyRequest(TargetService.PAYROLL, HttpMethod.POST,
                URI.create("/api/v1/payroll/daily-payments"), headers(), "{\"paidAmount\":80000}".getBytes(StandardCharsets.UTF_8)));

        assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
        assertThat(payrollHandler.requestBody).contains("80000");
    }

    @Test
    void rejectsPayrollMutationWhenUserDoesNotHaveManagePermission() throws IOException {
        startIdentityServer("[\"PAYROLL_VIEW\"]");
        CapturingHandler payrollHandler = startPayrollServer();
        RestClientInternalServiceGateway gateway = gateway();

        assertThatThrownBy(() -> gateway.exchange(new ProxyRequest(TargetService.PAYROLL, HttpMethod.POST,
                URI.create("/api/v1/payroll/daily-payments"), headers(), new byte[0])))
                .isInstanceOf(BffAccessDeniedException.class);
        assertThat(payrollHandler.requestBody).isNull();
    }

    @Test
    void allowsSaleConfirmationWhenUserOnlyHasSalesCreatePermission() throws IOException {
        startIdentityServer("[\"SALES_CREATE\"]");
        CapturingHandler billingHandler = startBillingServer("/api/v1/sales");
        RestClientInternalServiceGateway gateway = gateway();

        ProxyResponse response = gateway.exchange(new ProxyRequest(TargetService.BILLING, HttpMethod.POST,
                URI.create("/api/v1/sales/" + SALE_ID + "/confirm"), headers(), new byte[0]));

        assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
        assertThat(billingHandler.requestPath).isEqualTo("/api/v1/sales/" + SALE_ID + "/confirm");
    }

    @Test
    void rejectsFiscalConfigurationMutationWhenSalesUserDoesNotHaveFiscalPermission() throws IOException {
        startIdentityServer("[\"SALES_CREATE\"]");
        CapturingHandler billingHandler = startBillingServer("/api/v1/issuers");
        RestClientInternalServiceGateway gateway = gateway();

        assertThatThrownBy(() -> gateway.exchange(new ProxyRequest(TargetService.BILLING, HttpMethod.POST,
                URI.create("/api/v1/issuers"), headers(), "{}".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(BffAccessDeniedException.class);
        assertThat(billingHandler.requestBody).isNull();
    }

    @Test
    void allowsFiscalConfigurationWhenUserHasFiscalSettingsPermission() throws IOException {
        startIdentityServer("[\"FISCAL_SETTINGS_MANAGE\"]");
        CapturingHandler billingHandler = startBillingServer("/api/v1/fiscal-policy");
        RestClientInternalServiceGateway gateway = gateway();

        ProxyResponse response = gateway.exchange(new ProxyRequest(TargetService.BILLING, HttpMethod.PUT,
                URI.create("/api/v1/fiscal-policy"), headers(), "{}".getBytes(StandardCharsets.UTF_8)));

        assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
        assertThat(billingHandler.requestPath).isEqualTo("/api/v1/fiscal-policy");
    }

    @Test
    void fiscalSettingsPermissionDoesNotAuthorizeDianDocumentSubmission() throws IOException {
        startIdentityServer("[\"FISCAL_SETTINGS_MANAGE\"]");
        RestClientInternalServiceGateway gateway = gateway();

        assertThatThrownBy(() -> gateway.exchange(new ProxyRequest(TargetService.DIAN_PROVIDER, HttpMethod.POST,
                URI.create("/api/v1/provider/electronic-invoices"), headers(), "{}".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(BffAccessDeniedException.class);
    }

    @Test
    void rejectsDianConfigurationWhenPathDoesNotMatchCompanyHeader() throws IOException {
        startIdentityServer("[\"FISCAL_SETTINGS_MANAGE\"]");
        String otherCompanyId = "99999999-9999-9999-9999-999999999999";
        RestClientInternalServiceGateway gateway = gateway();

        assertThatThrownBy(() -> gateway.exchange(new ProxyRequest(TargetService.DIAN_PROVIDER, HttpMethod.PUT,
                URI.create("/api/v1/dian-configuration/companies/" + otherCompanyId), headers(),
                "{}".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(BffAccessDeniedException.class);
    }

    @Test
    void fiscalRulesRequireDedicatedFiscalSettingsPermission() throws IOException {
        startIdentityServer("[\"ACCOUNTING_MANAGE\"]");
        CapturingHandler accountingHandler = startAccountingServer("/api/v1/fiscal-catalog/rules");
        RestClientInternalServiceGateway gateway = gateway();

        assertThatThrownBy(() -> gateway.exchange(new ProxyRequest(TargetService.ACCOUNTING, HttpMethod.POST,
                URI.create("/api/v1/fiscal-catalog/rules"), headers(), "{}".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(BffAccessDeniedException.class);
        assertThat(accountingHandler.requestBody).isNull();
    }

    @Test
    void allowsFiscalRulesWhenUserHasFiscalSettingsPermission() throws IOException {
        startIdentityServer("[\"FISCAL_SETTINGS_MANAGE\"]");
        startLicenseServer(true);
        CapturingHandler accountingHandler = startAccountingServer("/api/v1/fiscal-catalog/rules");
        RestClientInternalServiceGateway gateway = gateway();

        ProxyResponse response = gateway.exchange(new ProxyRequest(TargetService.ACCOUNTING, HttpMethod.POST,
                URI.create("/api/v1/fiscal-catalog/rules"), headers(), "{}".getBytes(StandardCharsets.UTF_8)));

        assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
        assertThat(accountingHandler.requestPath).isEqualTo("/api/v1/fiscal-catalog/rules");
    }

    @Test
    void allowsCompanyTaxProfileWithFiscalSettingsPermission() throws IOException {
        startIdentityServer("[\"FISCAL_SETTINGS_MANAGE\"]");
        String path = "/api/v1/companies/" + COMPANY_ID + "/tax-profile";
        CapturingHandler tenantHandler = startTenantServer(path);
        RestClientInternalServiceGateway gateway = gateway();

        ProxyResponse response = gateway.exchange(new ProxyRequest(TargetService.TENANT, HttpMethod.PUT,
                URI.create(path), headers(), "{}".getBytes(StandardCharsets.UTF_8)));

        assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
        assertThat(tenantHandler.requestPath).isEqualTo(path);
    }

    @Test
    void rejectsTenantMutationWhenPathDoesNotMatchCompanyHeader() throws IOException {
        startIdentityServer("[\"COMPANY_SETTINGS_MANAGE\"]");
        String otherCompanyId = "99999999-9999-9999-9999-999999999999";
        CapturingHandler tenantHandler = startTenantServer("/api/v1/companies/" + otherCompanyId);
        RestClientInternalServiceGateway gateway = gateway();

        assertThatThrownBy(() -> gateway.exchange(new ProxyRequest(TargetService.TENANT, HttpMethod.PUT,
                URI.create("/api/v1/companies/" + otherCompanyId), headers(), "{}".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(BffAccessDeniedException.class);
        assertThat(tenantHandler.requestBody).isNull();
    }

    @Test
    void allowsPurchaseMutationWhenUserHasPurchasesManagePermission() throws IOException {
        startIdentityServer("[\"PURCHASES_MANAGE\"]");
        startLicenseServer(true);
        CapturingHandler inventoryHandler = startInventoryServer("/api/v1/purchases");
        RestClientInternalServiceGateway gateway = gateway();

        ProxyResponse response = gateway.exchange(new ProxyRequest(TargetService.INVENTORY, HttpMethod.POST,
                URI.create("/api/v1/purchases"), headers(), "{\"total\":1000}".getBytes(StandardCharsets.UTF_8)));

        assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
        assertThat(inventoryHandler.requestBody).contains("1000");
    }

    @Test
    void rejectsAsyncReportWhenLicenseDoesNotIncludeFeature() throws IOException {
        startIdentityServer("[\"REPORTS_VIEW\"]");
        startLicenseServer(false);
        CapturingHandler reportingHandler = startReportingServer("/api/v1/reports/export-jobs");
        RestClientInternalServiceGateway gateway = gateway();

        assertThatThrownBy(() -> gateway.exchange(new ProxyRequest(TargetService.REPORTING, HttpMethod.POST,
                URI.create("/api/v1/reports/export-jobs"), headers(), "{}".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(BffAccessDeniedException.class)
                .hasMessageContaining("licencia");
        assertThat(reportingHandler.requestBody).isNull();
    }

    @Test
    void rejectsSupplierCreationWhenLicenseOnlyIncludesCustomers() throws IOException {
        startIdentityServer("[\"COMPANY_SETTINGS_MANAGE\"]");
        startLicenseServer(false);
        CapturingHandler thirdpartyHandler = startThirdpartyServer("/api/v1/third-parties");
        RestClientInternalServiceGateway gateway = gateway();

        assertThatThrownBy(() -> gateway.exchange(new ProxyRequest(TargetService.THIRDPARTY, HttpMethod.POST,
                URI.create("/api/v1/third-parties"), headers(),
                "{\"roles\":[\"SUPPLIER\"]}".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(BffAccessDeniedException.class);
        assertThat(thirdpartyHandler.requestBody).isNull();
    }

    @Test
    void rejectsPurchaseMutationWhenSalesUserDoesNotHavePurchasesManagePermission() throws IOException {
        startIdentityServer("[\"SALES_CREATE\"]");
        CapturingHandler inventoryHandler = startInventoryServer("/api/v1/purchases");
        RestClientInternalServiceGateway gateway = gateway();

        assertThatThrownBy(() -> gateway.exchange(new ProxyRequest(TargetService.INVENTORY, HttpMethod.POST,
                URI.create("/api/v1/purchases"), headers(), "{}".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(BffAccessDeniedException.class);
        assertThat(inventoryHandler.requestBody).isNull();
    }

    @Test
    void allowsCompanyBrandingMutationWhenUserHasCompanySettingsPermission() throws IOException {
        startIdentityServer("[\"COMPANY_SETTINGS_MANAGE\"]");
        CapturingHandler tenantHandler = startTenantServer("/api/v1/companies/" + COMPANY_ID + "/branding");
        RestClientInternalServiceGateway gateway = gateway();

        ProxyResponse response = gateway.exchange(new ProxyRequest(TargetService.TENANT, HttpMethod.PUT,
                URI.create("/api/v1/companies/" + COMPANY_ID + "/branding"), headers(),
                "{\"displayName\":\"Necto Cafe\"}".getBytes(StandardCharsets.UTF_8)));

        assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
        assertThat(tenantHandler.requestPath).isEqualTo("/api/v1/companies/" + COMPANY_ID + "/branding");
        assertThat(tenantHandler.requestBody).contains("Necto Cafe");
    }

    @Test
    void rejectsCompanyBrandingMutationWhenUserDoesNotHaveCompanySettingsPermission() throws IOException {
        startIdentityServer("[\"SALES_CREATE\"]");
        CapturingHandler tenantHandler = startTenantServer("/api/v1/companies/" + COMPANY_ID + "/branding");
        RestClientInternalServiceGateway gateway = gateway();

        assertThatThrownBy(() -> gateway.exchange(new ProxyRequest(TargetService.TENANT, HttpMethod.PUT,
                URI.create("/api/v1/companies/" + COMPANY_ID + "/branding"), headers(),
                "{}".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(BffAccessDeniedException.class);
        assertThat(tenantHandler.requestBody).isNull();
    }

    @Test
    void writesAuditEventForSuccessfulMutationWithoutSensitiveHeaders() throws IOException {
        startIdentityServer("[\"SALES_CREATE\"]");
        CapturingHandler billingHandler = startBillingServer("/api/v1/sales");
        CapturingHandler auditHandler = startAuditServer();
        RestClientInternalServiceGateway gateway = gateway();

        ProxyResponse response = gateway.exchange(new ProxyRequest(TargetService.BILLING, HttpMethod.POST,
                URI.create("/api/v1/sales/" + SALE_ID + "/confirm"), headers(), new byte[0]));

        assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
        assertThat(billingHandler.requestPath).isEqualTo("/api/v1/sales/" + SALE_ID + "/confirm");
        assertThat(auditHandler.companyId).isEqualTo(COMPANY_ID);
        assertThat(auditHandler.requestBody).contains("\"eventType\":\"BFF_MUTATION\"");
        assertThat(auditHandler.requestBody).contains("\"resourceType\":\"BILLING\"");
        assertThat(auditHandler.requestBody).contains("\"result\":\"SUCCESS\"");
        assertThat(auditHandler.requestBody).contains("corr-bff-rbac");
        assertThat(auditHandler.requestBody).doesNotContain("token");
    }

    @Test
    void allowsRootToReadGlobalCatalogsWithoutCompanyContext() throws IOException {
        startRootIdentityServer();
        CapturingHandler catalogHandler = startCatalogServer("/api/v1/catalogs/DIAN_DOCUMENT_TYPE/items");
        RestClientInternalServiceGateway gateway = gateway();

        ProxyResponse response = gateway.exchange(new ProxyRequest(TargetService.CATALOG, HttpMethod.GET,
                URI.create("/api/v1/catalogs/DIAN_DOCUMENT_TYPE/items"), rootHeaders(), new byte[0]));

        assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
        assertThat(catalogHandler.requestPath).isEqualTo("/api/v1/catalogs/DIAN_DOCUMENT_TYPE/items");
        assertThat(catalogHandler.companyId).isNull();
    }

    private RestClientInternalServiceGateway gateway() {
        String identityUrl = "http://localhost:" + identityServer.getAddress().getPort();
        String tenantUrl = serverUrl(tenantServer, "http://tenant");
        String payrollUrl = serverUrl(payrollServer, "http://payroll");
        String billingUrl = serverUrl(billingServer, "http://billing");
        String inventoryUrl = serverUrl(inventoryServer, "http://inventory");
        String auditUrl = serverUrl(auditServer, "http://audit");
        String catalogUrl = serverUrl(catalogServer, "http://catalog");
        String accountingUrl = serverUrl(accountingServer, "http://accounting");
        String reportingUrl = serverUrl(reportingServer, "http://reporting");
        String thirdpartyUrl = serverUrl(thirdpartyServer, "http://thirdparty");
        BffProperties properties = new BffProperties(tenantUrl, identityUrl, catalogUrl, thirdpartyUrl,
                inventoryUrl, billingUrl, accountingUrl, payrollUrl, reportingUrl, "http://dian",
                auditUrl);
        return new RestClientInternalServiceGateway(RestClient.builder(), properties, new ObjectMapper());
    }

    private static String serverUrl(HttpServer server, String fallback) {
        if (server == null) {
            return fallback;
        }
        return "http://localhost:" + server.getAddress().getPort();
    }

    private void startIdentityServer(String permissionsJson) throws IOException {
        identityServer = HttpServer.create(new InetSocketAddress(0), 0);
        identityServer.createContext("/api/v1/platform/permissions", exchange -> {
            exchange.sendResponseHeaders(403, -1);
            exchange.close();
        });
        identityServer.createContext("/api/v1/me", exchange -> {
            byte[] body = ("{\"id\":\"" + USER_ID + "\"}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        identityServer.createContext("/api/v1/companies/" + COMPANY_ID + "/permissions", exchange -> {
            byte[] body = ("{\"companyId\":\"" + COMPANY_ID + "\",\"roles\":[\"VENDEDOR\"],\"permissions\":"
                    + permissionsJson + "}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        identityServer.start();
    }

    private void startRootIdentityServer() throws IOException {
        identityServer = HttpServer.create(new InetSocketAddress(0), 0);
        identityServer.createContext("/api/v1/platform/permissions", exchange -> {
            byte[] body = "[]".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        identityServer.start();
    }

    private CapturingHandler startPayrollServer() throws IOException {
        payrollServer = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        payrollServer.createContext("/api/v1/payroll/daily-payments", handler::handle);
        payrollServer.start();
        return handler;
    }

    private CapturingHandler startTenantServer(String path) throws IOException {
        tenantServer = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        tenantServer.createContext(path, handler::handle);
        tenantServer.start();
        return handler;
    }

    private void startLicenseServer(boolean allowed) throws IOException {
        tenantServer = HttpServer.create(new InetSocketAddress(0), 0);
        tenantServer.createContext("/api/v1/companies/" + COMPANY_ID + "/license/validation", exchange -> {
            byte[] body = ("{\"allowed\":" + allowed + ",\"message\":\"Funcionalidad no incluida en la licencia.\"}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        tenantServer.start();
    }

    private CapturingHandler startBillingServer(String path) throws IOException {
        billingServer = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        billingServer.createContext(path, handler::handle);
        billingServer.start();
        return handler;
    }

    private CapturingHandler startInventoryServer(String path) throws IOException {
        inventoryServer = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        inventoryServer.createContext(path, handler::handle);
        inventoryServer.start();
        return handler;
    }

    private CapturingHandler startAuditServer() throws IOException {
        auditServer = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        auditServer.createContext("/api/v1/audit-events", handler::handle);
        auditServer.start();
        return handler;
    }

    private CapturingHandler startCatalogServer(String path) throws IOException {
        catalogServer = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        catalogServer.createContext(path, handler::handle);
        catalogServer.start();
        return handler;
    }

    private CapturingHandler startAccountingServer(String path) throws IOException {
        accountingServer = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        accountingServer.createContext(path, handler::handle);
        accountingServer.start();
        return handler;
    }

    private CapturingHandler startReportingServer(String path) throws IOException {
        reportingServer = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        reportingServer.createContext(path, handler::handle);
        reportingServer.start();
        return handler;
    }

    private CapturingHandler startThirdpartyServer(String path) throws IOException {
        thirdpartyServer = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        thirdpartyServer.createContext(path, handler::handle);
        thirdpartyServer.start();
        return handler;
    }

    private static HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        headers.set("X-Company-Id", COMPANY_ID);
        headers.set("X-User-Id", USER_ID);
        headers.set("X-Correlation-Id", "corr-bff-rbac");
        return headers;
    }

    private static HttpHeaders rootHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("root-token");
        headers.set("X-Correlation-Id", "corr-bff-root");
        return headers;
    }

    private static final class CapturingHandler {
        private String requestBody;
        private String requestPath;
        private String companyId;

        private void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            requestPath = exchange.getRequestURI().getPath();
            companyId = exchange.getRequestHeaders().getFirst("X-Company-Id");
            requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] body = "{\"id\":\"payment-1\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(201, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        }
    }
}
