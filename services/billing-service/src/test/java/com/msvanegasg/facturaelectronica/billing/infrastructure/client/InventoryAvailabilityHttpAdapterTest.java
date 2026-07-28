package com.msvanegasg.facturaelectronica.billing.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.billing.domain.model.SaleItemType;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;
import com.sun.net.httpserver.HttpServer;

class InventoryAvailabilityHttpAdapterTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void fetchesProductSnapshotByHttp() throws IOException {
        UUID productId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        server = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        server.createContext("/api/v1/products/" + productId, handler::handleProduct);
        server.start();
        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        InventoryAvailabilityHttpAdapter adapter = new InventoryAvailabilityHttpAdapter(
                new BillingProperties(baseUrl, "http://provider", "http://accounting", "http://audit", "http://tenant", "ACCEPTED"));

        var result = adapter.findProduct(UUID.fromString("11111111-1111-1111-1111-111111111111"), productId);

        assertThat(handler.companyId).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(result.itemType()).isEqualTo(SaleItemType.SERVICE);
        assertThat(result.stockTracked()).isFalse();
        assertThat(result.name()).isEqualTo("Manicura");
    }

    private static final class CapturingHandler {

        private String companyId;

        private void handleProduct(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            companyId = exchange.getRequestHeaders().getFirst("X-Company-Id");
            byte[] response = """
                    {
                      "id": "33333333-3333-3333-3333-333333333333",
                      "companyId": "11111111-1111-1111-1111-111111111111",
                      "sku": "SERV-1",
                      "name": "Manicura",
                      "itemType": "SERVICE",
                      "saleEnabled": true,
                      "purchaseEnabled": false,
                      "stockTracked": false,
                      "salePrice": 35000.00,
                      "cost": 0.00,
                      "active": true,
                      "currentStock": 0.00
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }
    }
}
