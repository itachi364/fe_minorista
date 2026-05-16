package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.inventory.application.dto.CheckStockAvailabilityCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.StockAvailabilityResult;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.StockBalanceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.StockBalance;

class CheckStockAvailabilityServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");
    private static final UUID PRODUCT_ID = UUID.fromString("99998888-7777-6666-5555-444433332222");

    @Test
    void checkReturnsAvailableWhenCurrentStockCoversRequestedQuantity() {
        InMemoryStockBalanceRepository repository = new InMemoryStockBalanceRepository();
        repository.save(StockBalance.restore(
                COMPANY_ID,
                PRODUCT_ID,
                new BigDecimal("10.0000"),
                new BigDecimal("2.0000")));
        CheckStockAvailabilityService service = new CheckStockAvailabilityService(repository);

        StockAvailabilityResult result = service.check(command("8.0000"));

        assertThat(result.available()).isTrue();
        assertThat(result.availableStock()).isEqualByComparingTo("8.0000");
        assertThat(result.requestedQuantity()).isEqualByComparingTo("8.0000");
    }

    @Test
    void checkReturnsUnavailableWhenRequestedQuantityExceedsAvailableStock() {
        InMemoryStockBalanceRepository repository = new InMemoryStockBalanceRepository();
        repository.save(StockBalance.restore(
                COMPANY_ID,
                PRODUCT_ID,
                new BigDecimal("5.0000"),
                BigDecimal.ZERO));
        CheckStockAvailabilityService service = new CheckStockAvailabilityService(repository);

        StockAvailabilityResult result = service.check(command("6.0000"));

        assertThat(result.available()).isFalse();
        assertThat(result.availableStock()).isEqualByComparingTo("5.0000");
    }

    @Test
    void requireAvailableRejectsInsufficientStock() {
        CheckStockAvailabilityService service = new CheckStockAvailabilityService(new InMemoryStockBalanceRepository());

        assertThatThrownBy(() -> service.requireAvailable(command("1.0000")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("stock is insufficient for requested quantity");
    }

    @Test
    void checkRejectsNonPositiveRequestedQuantity() {
        CheckStockAvailabilityService service = new CheckStockAvailabilityService(new InMemoryStockBalanceRepository());

        assertThatThrownBy(() -> service.check(command("0.0000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("requestedQuantity must be greater than zero");
    }

    private static CheckStockAvailabilityCommand command(String requestedQuantity) {
        return new CheckStockAvailabilityCommand(
                COMPANY_ID,
                PRODUCT_ID,
                new BigDecimal(requestedQuantity));
    }

    private static final class InMemoryStockBalanceRepository implements StockBalanceRepositoryPort {

        private final Map<String, StockBalance> balances = new HashMap<>();

        @Override
        public Optional<StockBalance> findByCompanyIdAndProductId(UUID companyId, UUID productId) {
            return Optional.ofNullable(balances.get(key(companyId, productId)));
        }

        @Override
        public StockBalance save(StockBalance stockBalance) {
            balances.put(key(stockBalance.companyId(), stockBalance.productId()), stockBalance);
            return stockBalance;
        }

        private static String key(UUID companyId, UUID productId) {
            return companyId + ":" + productId;
        }
    }
}
