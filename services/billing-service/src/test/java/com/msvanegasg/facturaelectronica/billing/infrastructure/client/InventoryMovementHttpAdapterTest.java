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

import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleItemType;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleLine;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;
import com.sun.net.httpserver.HttpServer;

class InventoryMovementHttpAdapterTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void appliesSaleOutMovementByHttp() throws IOException {
        UUID productId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        server = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        server.createContext("/api/v1/products/" + productId, handler::handleProduct);
        server.createContext("/api/v1/inventory-movements", handler::handleMovement);
        server.start();
        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        InventoryMovementHttpAdapter adapter = new InventoryMovementHttpAdapter(
                new BillingProperties(baseUrl, "http://provider", "http://accounting", "http://audit", "http://tenant", "ACCEPTED"));

        adapter.applySaleOut(sale(productId), "confirm-1");

        assertThat(handler.companyId).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(handler.idempotencyKey).isEqualTo("confirm-1-inventory-44444444-4444-4444-4444-444444444444");
        assertThat(handler.contentType).contains("application/json");
        assertThat(handler.movementRequestBody).contains("\"movementType\":\"SALE_OUT\"");
        assertThat(handler.movementRequestBody).contains("\"unitCost\":9000.00");
    }

    @Test
    void skipsSaleOutForServiceLines() throws IOException {
        UUID serviceProductId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        server = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        server.createContext("/api/v1/products/" + serviceProductId, handler::handleProduct);
        server.createContext("/api/v1/inventory-movements", handler::handleMovement);
        server.start();
        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        InventoryMovementHttpAdapter adapter = new InventoryMovementHttpAdapter(
                new BillingProperties(baseUrl, "http://provider", "http://accounting", "http://audit", "http://tenant", "ACCEPTED"));

        adapter.applySaleOut(serviceSale(serviceProductId), "confirm-1");

        assertThat(handler.movementRequestBody).isNull();
    }

    private static Sale sale(UUID productId) {
        return Sale.draft(UUID.fromString("22222222-2222-2222-2222-222222222222"),
                UUID.fromString("11111111-1111-1111-1111-111111111111"), null, null, SaleChannel.POS, "sale-1", null,
                Instant.parse("2026-05-19T10:00:00Z"),
                List.of(SaleLine.calculate(UUID.fromString("44444444-4444-4444-4444-444444444444"), productId,
                        new BigDecimal("2.00"), new BigDecimal("15000.00"), BigDecimal.ZERO, "IVA_19",
                        new BigDecimal("19.00"))));
    }

    private static Sale serviceSale(UUID productId) {
        return Sale.draft(UUID.fromString("22222222-2222-2222-2222-222222222222"),
                UUID.fromString("11111111-1111-1111-1111-111111111111"), null, null, SaleChannel.POS, "sale-1", null,
                Instant.parse("2026-05-19T10:00:00Z"),
                List.of(SaleLine.calculate(UUID.fromString("44444444-4444-4444-4444-444444444444"), productId,
                        "SERV-1", "Manicura", SaleItemType.SERVICE, false, BigDecimal.ONE,
                        new BigDecimal("35000.00"), BigDecimal.ZERO, "IVA_19", new BigDecimal("19.00"))));
    }

    private static final class CapturingHandler {

        private String companyId;
        private String idempotencyKey;
        private String contentType;
        private String movementRequestBody;

        private void handleProduct(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            companyId = exchange.getRequestHeaders().getFirst("X-Company-Id");
            byte[] response = """
                    {
                      "id": "33333333-3333-3333-3333-333333333333",
                      "companyId": "11111111-1111-1111-1111-111111111111",
                      "sku": "SKU-1",
                      "name": "Producto",
                      "itemType": "PHYSICAL_GOOD",
                      "saleEnabled": true,
                      "purchaseEnabled": true,
                      "stockTracked": true,
                      "salePrice": 15000.00,
                      "cost": 9000.00,
                      "active": true,
                      "currentStock": 10.00
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }

        private void handleMovement(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            idempotencyKey = exchange.getRequestHeaders().getFirst("Idempotency-Key");
            contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            movementRequestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(201, -1);
            exchange.close();
        }
    }
}
