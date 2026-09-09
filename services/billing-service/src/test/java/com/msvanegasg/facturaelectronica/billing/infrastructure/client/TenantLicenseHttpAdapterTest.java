package com.msvanegasg.facturaelectronica.billing.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;
import com.sun.net.httpserver.HttpServer;

class TenantLicenseHttpAdapterTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void enablesAutomaticAccountingSetupOnlyForPosPlan() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        LicenseHandler handler = new LicenseHandler();
        server.createContext("/api/v1/companies/", handler::handle);
        server.start();
        TenantLicenseHttpAdapter adapter = new TenantLicenseHttpAdapter(properties());

        handler.planCode = "POS";
        assertThat(adapter.allowsAutomaticAccountingSetup(COMPANY_ID)).isTrue();
        handler.planCode = "FULL";
        assertThat(adapter.allowsAutomaticAccountingSetup(COMPANY_ID)).isFalse();

        assertThat(handler.lastQuery).contains("action=CREATE_TRANSACTION")
                .contains("module=BILLING")
                .contains("feature=POS_SALES");
    }

    private BillingProperties properties() {
        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        return new BillingProperties("http://inventory", "http://provider", "http://accounting", "http://audit",
                baseUrl, "http://identity", "ACCEPTED");
    }

    private static final class LicenseHandler {

        private String planCode;
        private String lastQuery;

        private void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            lastQuery = exchange.getRequestURI().getRawQuery();
            String body = """
                    {"companyId":"%s","action":"CREATE_TRANSACTION","module":"BILLING","feature":"POS_SALES",
                     "allowed":true,"status":"ACTIVE","planCode":"%s","maxUsers":5,
                     "maxMonthlyDocuments":1000,"reasonCode":"LICENSE_ACTIVE","message":"Permitido"}
                    """.formatted(COMPANY_ID, planCode);
            byte[] response = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }
    }
}
