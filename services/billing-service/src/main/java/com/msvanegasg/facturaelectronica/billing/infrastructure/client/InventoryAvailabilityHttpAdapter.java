package com.msvanegasg.facturaelectronica.billing.infrastructure.client;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.billing.application.port.out.InventoryAvailabilityPort;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;

@Component
public class InventoryAvailabilityHttpAdapter implements InventoryAvailabilityPort {

    private final RestClient restClient;

    public InventoryAvailabilityHttpAdapter(BillingProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.inventoryServiceUrl()).build();
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
}
