package com.msvanegasg.facturaelectronica.payroll.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.payroll.domain.model.DailyLaborPayment;
import com.sun.net.httpserver.HttpServer;

class PayrollAccountingHttpAdapterTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void postsDailyPaymentAccountingEntryByHttp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        server.createContext("/api/v1/accounting-entries", handler::handle);
        server.start();
        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        PayrollAccountingHttpAdapter adapter = new PayrollAccountingHttpAdapter(RestClient.builder(), baseUrl);

        adapter.applyDailyPayment(payment());

        assertThat(handler.companyId).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(handler.idempotencyKey).isEqualTo("payroll-daily-payment-33333333-3333-3333-3333-333333333333");
        assertThat(handler.contentType).contains("application/json");
        assertThat(handler.requestBody).contains("\"eventType\":\"PAYROLL_DAILY_PAYMENT_REGISTERED\"");
        assertThat(handler.requestBody).contains("\"sourceType\":\"PAYROLL_DAILY_PAYMENT\"");
        assertThat(handler.requestBody).contains("\"entryDate\":\"2026-08-11\"");
        assertThat(handler.requestBody).contains("\"total\":80000.00");
    }

    @Test
    void ignoresAccountingWhenBaseUrlIsBlank() {
        PayrollAccountingHttpAdapter adapter = new PayrollAccountingHttpAdapter(RestClient.builder(), "");

        adapter.applyDailyPayment(payment());
    }

    private static DailyLaborPayment payment() {
        return new DailyLaborPayment(UUID.fromString("33333333-3333-3333-3333-333333333333"),
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"), LocalDate.parse("2026-08-11"),
                "Apoyo en ventas", new BigDecimal("80000.00"), new BigDecimal("80000.00"), "CASH", true,
                "Pago al finalizar el dia", Instant.parse("2026-08-11T10:00:00Z"));
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
