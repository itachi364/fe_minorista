package com.msvanegasg.facturaelectronica.inventory.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class StockBalanceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    @Test
    void purchaseInIncreasesStockAndUpdatesCost() {
        StockBalance balance = StockBalance.empty(COMPANY_ID, PRODUCT_ID, NOW)
                .apply(InventoryMovementType.PURCHASE_IN, new BigDecimal("5.00"), new BigDecimal("1200.00"), NOW);

        assertThat(balance.currentStock()).isEqualByComparingTo("5.00");
        assertThat(balance.averageCost()).isEqualByComparingTo("1200.00");
    }

    @Test
    void saleOutRejectsNegativeStock() {
        StockBalance balance = StockBalance.empty(COMPANY_ID, PRODUCT_ID, NOW);

        assertThatThrownBy(() -> balance.apply(InventoryMovementType.SALE_OUT, BigDecimal.ONE, BigDecimal.ZERO, NOW))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("stock is insufficient for movement");
    }
}
