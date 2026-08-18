package com.msvanegasg.facturaelectronica.inventory.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.inventory.domain.model.PaymentCondition;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseLine;
import com.sun.net.httpserver.HttpServer;

class PurchaseAccountingHttpAdapterTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PURCHASE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SUPPLIER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID USER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final Instant CONFIRMED_AT = Instant.parse("2026-08-18T10:15:30Z");

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void postsAccountingEntryAndPayableForCreditPurchase() throws IOException {
        CapturingHandler entryHandler = new CapturingHandler(201);
        CapturingHandler payableHandler = new CapturingHandler(201);
        startServer(entryHandler, payableHandler);

        adapter().applyConfirmedPurchase(confirmedPurchase(), USER_ID);

        assertThat(entryHandler.requestBody).contains("\"eventType\":\"PURCHASE_CONFIRMED\"");
        assertThat(entryHandler.requestBody).contains("\"sourceType\":\"PURCHASE\"");
        assertThat(entryHandler.requestBody).contains("\"sourceId\":\"" + PURCHASE_ID + "\"");
        assertThat(entryHandler.requestBody).contains("\"entryDate\":\"2026-08-18\"");
        assertThat(entryHandler.companyId).isEqualTo(COMPANY_ID.toString());
        assertThat(payableHandler.requestBody).contains("\"sourceType\":\"PURCHASE\"");
        assertThat(payableHandler.requestBody).contains("\"sourceId\":\"" + PURCHASE_ID + "\"");
        assertThat(payableHandler.requestBody).contains("\"totalAmount\":53550.00");
    }

    @Test
    void propagatesAccountingFailuresSoPurchaseFlowIsVerifiable() throws IOException {
        startServer(new CapturingHandler(500), new CapturingHandler(201));

        assertThatThrownBy(() -> adapter().applyConfirmedPurchase(confirmedPurchase(), USER_ID))
                .isInstanceOf(HttpServerErrorException.class);
    }

    private PurchaseAccountingHttpAdapter adapter() {
        return new PurchaseAccountingHttpAdapter(RestClient.builder(), "http://localhost:" + server.getAddress().getPort());
    }

    private void startServer(CapturingHandler entryHandler, CapturingHandler payableHandler) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/v1/accounting-entries", entryHandler::handle);
        server.createContext("/api/v1/accounts-payable", payableHandler::handle);
        server.start();
    }

    private static Purchase confirmedPurchase() {
        return Purchase.pending(PURCHASE_ID, COMPANY_ID, SUPPLIER_ID, new BigDecimal("45000.00"),
                new BigDecimal("8550.00"), new BigDecimal("53550.00"), PaymentCondition.CREDIT,
                LocalDate.of(2026, 12, 31), null, "purchase-1", Instant.parse("2026-08-18T10:00:00Z"),
                List.of(new PurchaseLine(UUID.randomUUID(), PURCHASE_ID, UUID.randomUUID(), BigDecimal.ONE,
                        BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN)))
                .confirm(CONFIRMED_AT);
    }

    private static final class CapturingHandler {

        private final int status;
        private String requestBody;
        private String companyId;

        private CapturingHandler(int status) {
            this.status = status;
        }

        private void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            companyId = exchange.getRequestHeaders().getFirst("X-Company-Id");
            requestBody = new String(exchange.getRequestBody().readAllBytes());
            byte[] body = "{}".getBytes();
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        }
    }
}
