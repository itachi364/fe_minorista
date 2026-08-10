package com.msvanegasg.facturaelectronica.billing.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.PaymentMethodCode;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleLine;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;
import com.sun.net.httpserver.HttpServer;

class DianProviderHttpAdapterTest {

    private static final UUID DOCUMENT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void submitsElectronicPosByHttp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        server.createContext("/api/v1/provider/electronic-pos", handler::handle);
        server.start();
        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        DianProviderHttpAdapter adapter = new DianProviderHttpAdapter(
                new BillingProperties("http://inventory", baseUrl, "http://accounting", "http://audit", "http://tenant", "ACCEPTED"));

        var result = adapter.submit(sale(), DOCUMENT_ID, ElectronicDocumentType.ELECTRONIC_POS, "confirm-1");

        assertThat(result.status()).isEqualTo(ProviderStatus.ACCEPTED);
        assertThat(result.trackingId()).isEqualTo("mock-tracking");
        assertThat(result.cufeCude()).isEqualTo("mock-cude");
        assertThat(handler.requestBody).contains("\"companyId\"");
        assertThat(handler.requestBody).contains("\"documentId\":\"" + DOCUMENT_ID + "\"");
        assertThat(handler.idempotencyKey).isEqualTo("confirm-1");
        assertThat(handler.contentType).contains("application/json");
    }

    private static Sale sale() {
        UUID companyId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID saleId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID productId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        return Sale.draft(saleId, companyId, null, PaymentMethodCode.CASH, null, SaleChannel.POS, "sale-1", null,
                Instant.parse("2026-05-19T10:00:00Z"),
                List.of(SaleLine.calculate(UUID.fromString("44444444-4444-4444-4444-444444444444"), productId,
                        new BigDecimal("2.00"), new BigDecimal("15000.00"), BigDecimal.ZERO, "IVA_19",
                        new BigDecimal("19.00"))));
    }

    private static final class CapturingHandler {

        private String requestBody;
        private String idempotencyKey;
        private String contentType;

        private void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            idempotencyKey = exchange.getRequestHeaders().getFirst("Idempotency-Key");
            contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] response = """
                    {
                      "trackingId": "mock-tracking",
                      "status": "ACCEPTED",
                      "cufeCude": "mock-cude",
                      "qrContent": "mock-qr",
                      "errorCode": null,
                      "errorMessage": null
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }
    }
}
