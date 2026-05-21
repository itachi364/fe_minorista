package com.msvanegasg.facturaelectronica.inventory.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ProductTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");

    @Test
    void createsPhysicalGoodWithStockTracking() {
        Product product = Product.create(PRODUCT_ID, COMPANY_ID, "SKU-1", null, "Cafe", null,
                InventoryItemType.PHYSICAL_GOOD, true, true, true, new BigDecimal("15000.00"),
                new BigDecimal("9000.00"), NOW);

        assertThat(product.itemType()).isEqualTo(InventoryItemType.PHYSICAL_GOOD);
        assertThat(product.stockTracked()).isTrue();
        assertThat(product.saleEnabled()).isTrue();
        assertThat(product.purchaseEnabled()).isTrue();
    }

    @Test
    void rejectsServiceWithAutomaticStockTracking() {
        assertThatThrownBy(() -> Product.create(PRODUCT_ID, COMPANY_ID, "SERV-1", null, "Manicura", null,
                InventoryItemType.SERVICE, true, false, true, new BigDecimal("35000.00"), BigDecimal.ZERO, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("services must not track stock automatically");
    }
}
