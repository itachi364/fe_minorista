package com.msvanegasg.facturaelectronica.billing.infrastructure.client;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.billing.application.dto.InventoryProductSnapshot;
import com.msvanegasg.facturaelectronica.billing.application.port.out.InventoryAvailabilityPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleItemType;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;

@Component
public class InventoryAvailabilityHttpAdapter implements InventoryAvailabilityPort {

    private final RestClient restClient;

    public InventoryAvailabilityHttpAdapter(BillingProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.inventoryServiceUrl()).build();
    }

    @Override
    public InventoryProductSnapshot findProduct(UUID companyId, UUID productId) {
        ProductResponse response = restClient.get()
                .uri("/api/v1/products/{productId}", productId)
                .header("X-Company-Id", companyId.toString())
                .retrieve()
                .body(ProductResponse.class);
        if (response == null) {
            throw new IllegalStateException("Inventory service did not return product data.");
        }
        SaleItemType itemType = response.itemType() == null ? SaleItemType.PHYSICAL_GOOD : response.itemType();
        return new InventoryProductSnapshot(response.id(), response.sku(), response.name(), itemType,
                response.saleEnabled(), response.stockTracked(), response.cost(), response.currentStock());
    }

    @Override
    public boolean isAvailable(UUID companyId, UUID productId, BigDecimal quantity) {
        StockAvailabilityResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/products/{productId}/availability")
                        .queryParam("quantity", quantity)
                        .build(productId))
                .header("X-Company-Id", companyId.toString())
                .retrieve()
                .body(StockAvailabilityResponse.class);
        return response != null && response.available();
    }

    record StockAvailabilityResponse(UUID companyId, UUID productId, BigDecimal requestedQuantity,
            BigDecimal availableQuantity, boolean available) {
    }

    record ProductResponse(UUID id, UUID companyId, String sku, String barcode, String name, String description,
            SaleItemType itemType, boolean saleEnabled, boolean purchaseEnabled, boolean stockTracked,
            BigDecimal salePrice, BigDecimal cost, boolean active, BigDecimal currentStock) {
    }
}
