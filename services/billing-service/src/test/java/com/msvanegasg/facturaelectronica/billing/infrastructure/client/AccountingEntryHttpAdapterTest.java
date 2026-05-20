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

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleLine;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;
import com.sun.net.httpserver.HttpServer;

class AccountingEntryHttpAdapterTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void postsSaleAccountingEntryByHttp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        server.createContext("/api/v1/accounting-entries", handler::handle);
        server.start();
        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        AccountingEntryHttpAdapter adapter = new AccountingEntryHttpAdapter(
                new BillingProperties("http://inventory", "http://provider", baseUrl, "ACCEPTED"));

        adapter.postSale(confirmedSale(), "confirm-1");

        assertThat(handler.companyId).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(handler.idempotencyKey).isEqualTo("confirm-1-accounting");
        assertThat(handler.contentType).contains("application/json");
        assertThat(handler.requestBody).contains("\"eventType\":\"SALE_CONFIRMED\"");
        assertThat(handler.requestBody).contains("\"sourceType\":\"SALE\"");
        assertThat(handler.requestBody).contains("\"entryDate\":\"2026-05-19\"");
    }

    private static Sale confirmedSale() {
        UUID companyId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID saleId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Sale draft = Sale.draft(saleId, companyId, null, null, SaleChannel.POS, "sale-1", null,
                Instant.parse("2026-05-19T10:00:00Z"),
                List.of(SaleLine.calculate(UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        UUID.fromString("33333333-3333-3333-3333-333333333333"), new BigDecimal("2.00"),
                        new BigDecimal("15000.00"), BigDecimal.ZERO, "IVA_19", new BigDecimal("19.00"))));
        ElectronicDocument document = new ElectronicDocument(UUID.fromString("55555555-5555-5555-5555-555555555555"),
                companyId, saleId, ElectronicDocumentType.ELECTRONIC_POS, ElectronicDocumentStatus.VALIDATED,
                ProviderStatus.ACCEPTED, "POS", 1, "mock-cude", "mock-qr", draft.subtotal(), draft.taxTotal(),
                draft.total(), "mock-tracking", null, null, "confirm-1",
                Instant.parse("2026-05-19T10:01:00Z"), null, null);
        return draft.confirm(document, Instant.parse("2026-05-19T10:01:00Z"));
    }

    private static final class CapturingHandler {

        private String companyId;
        private String idempotencyKey;
        private String contentType;
        private String requestBody;

        private void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            companyId = exchange.getRequestHeaders().getFirst("X-Company-Id");
            idempotencyKey = exchange.getRequestHeaders().getFirst("Idempotency-Key");
            contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(201, -1);
            exchange.close();
        }
    }
}
