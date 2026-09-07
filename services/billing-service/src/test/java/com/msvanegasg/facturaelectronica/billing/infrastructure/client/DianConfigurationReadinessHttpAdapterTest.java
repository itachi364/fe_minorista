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

class DianConfigurationReadinessHttpAdapterTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void reportsReadyOnlyForActiveRealConfigurationWithSuccessfulTestAndCertificate() throws IOException {
        CapturingHandler handler = startServer("""
                {
                  "companyId": "11111111-1111-1111-1111-111111111111",
                  "mode": "REAL",
                  "certificateConfigured": true,
                  "status": "ACTIVE",
                  "lastTestStatus": "SUCCESS"
                }
                """, 200);
        DianConfigurationReadinessHttpAdapter adapter = adapter();

        assertThat(adapter.isReadyForElectronicIssuing(COMPANY_ID)).isTrue();
        assertThat(handler.path).isEqualTo("/api/v1/dian-configuration/companies/" + COMPANY_ID);
    }

    @Test
    void reportsNotReadyWhenConfigurationBelongsToAnotherCompany() throws IOException {
        startServer("""
                {
                  "companyId": "22222222-2222-2222-2222-222222222222",
                  "mode": "REAL",
                  "certificateConfigured": true,
                  "status": "ACTIVE",
                  "lastTestStatus": "SUCCESS"
                }
                """, 200);

        assertThat(adapter().isReadyForElectronicIssuing(COMPANY_ID)).isFalse();
    }

    @Test
    void reportsNotReadyWhenConfigurationIsMissingOrServiceFails() throws IOException {
        startServer("", 404);

        assertThat(adapter().isReadyForElectronicIssuing(COMPANY_ID)).isFalse();
    }

    private DianConfigurationReadinessHttpAdapter adapter() {
        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        return new DianConfigurationReadinessHttpAdapter(
                new BillingProperties("http://inventory", baseUrl, "http://accounting", "http://audit",
                        "http://tenant", "http://identity", "ACCEPTED"));
    }

    private CapturingHandler startServer(String body, int statusCode) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler(body, statusCode);
        server.createContext("/api/v1/dian-configuration/companies/" + COMPANY_ID, handler::handle);
        server.start();
        return handler;
    }

    private static final class CapturingHandler {

        private final String body;
        private final int statusCode;
        private String path;

        private CapturingHandler(String body, int statusCode) {
            this.body = body;
            this.statusCode = statusCode;
        }

        private void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            path = exchange.getRequestURI().getPath();
            byte[] response = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }
    }
}
