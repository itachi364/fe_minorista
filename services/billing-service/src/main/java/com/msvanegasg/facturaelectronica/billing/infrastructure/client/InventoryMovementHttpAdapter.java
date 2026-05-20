package com.msvanegasg.facturaelectronica.billing.infrastructure.client;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.billing.application.port.out.InventoryMovementPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleLine;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;

@Component
public class InventoryMovementHttpAdapter implements InventoryMovementPort {

    private final RestClient restClient;

    public InventoryMovementHttpAdapter(BillingProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.inventoryServiceUrl()).build();
    }

    @Override
    public void applySaleOut(Sale sale, String idempotencyKey) {
        sale.lines().forEach(line -> applyLine(sale, line, idempotencyKey));
    }

    private void applyLine(Sale sale, SaleLine line, String idempotencyKey) {
        ProductResponse product = product(sale.companyId(), line.productId());
        restClient.post()
                .uri("/api/v1/inventory-movements")
                .header("X-Company-Id", sale.companyId().toString())
                .header("Idempotency-Key", idempotencyKey + "-inventory-" + line.id())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new InventoryMovementRequest(line.productId(), "SALE_OUT", line.quantity(),
                        product.cost() == null ? BigDecimal.ZERO : product.cost(), "SALE", sale.id()))
                .retrieve()
                .toBodilessEntity();
    }

    private ProductResponse product(UUID companyId, UUID productId) {
        ProductResponse response = restClient.get()
                .uri("/api/v1/products/{productId}", productId)
                .header("X-Company-Id", companyId.toString())
                .retrieve()
                .body(ProductResponse.class);
        if (response == null) {
            throw new IllegalStateException("Inventory service did not return product data.");
        }
        return response;
    }

    record InventoryMovementRequest(UUID productId, String movementType, BigDecimal quantity, BigDecimal unitCost,
            String sourceDocumentType, UUID sourceDocumentId) {
    }

    record ProductResponse(UUID id, UUID companyId, String sku, String barcode, String name, String description,
            BigDecimal salePrice, BigDecimal cost, boolean active, BigDecimal currentStock) {
    }
}
