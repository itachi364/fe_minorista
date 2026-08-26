package com.msvanegasg.facturaelectronica.billing.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.billing.application.dto.AuditEventCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.AuditResult;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;
import com.sun.net.httpserver.HttpServer;

class AuditEventHttpAdapterTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void registersAuditEventByHttp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        server.createContext("/api/v1/audit-events", handler::handle);
        server.start();
        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        AuditEventHttpAdapter adapter = new AuditEventHttpAdapter(
                new BillingProperties("http://inventory", "http://provider", "http://accounting", baseUrl,
                        "http://tenant", "http://identity", "ACCEPTED"));

        adapter.register(new AuditEventCommand(UUID.fromString("11111111-1111-1111-1111-111111111111"), null,
                "ELECTRONIC_DOCUMENT", "SALE", "22222222-2222-2222-2222-222222222222", "CONFIRM_SALE",
                AuditResult.SUCCESS, "{\"status\":\"VALIDATED\"}"));

        assertThat(handler.companyId).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(handler.contentType).contains("application/json");
        assertThat(handler.requestBody).contains("\"eventType\":\"ELECTRONIC_DOCUMENT\"");
        assertThat(handler.requestBody).contains("\"resourceType\":\"SALE\"");
        assertThat(handler.requestBody).contains("\"action\":\"CONFIRM_SALE\"");
        assertThat(handler.requestBody).contains("\"result\":\"SUCCESS\"");
    }

    private static final class CapturingHandler {

        private String companyId;
        private String contentType;
        private String requestBody;

        private void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            companyId = exchange.getRequestHeaders().getFirst("X-Company-Id");
            contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] response = """
                    {
                      "id": "33333333-3333-3333-3333-333333333333",
                      "companyId": "11111111-1111-1111-1111-111111111111",
                      "eventType": "ELECTRONIC_DOCUMENT",
                      "resourceType": "SALE",
                      "resourceId": "22222222-2222-2222-2222-222222222222",
                      "action": "CONFIRM_SALE",
                      "result": "SUCCESS",
                      "occurredAt": "2026-05-20T10:00:00Z"
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }
    }
}
