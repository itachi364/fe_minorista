package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.RegisterInventoryMovementCommand;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.InventoryMovementRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.StockBalanceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovement;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.StockBalance;

class RegisterInventoryMovementServiceTest {

    private static final UUID MOVEMENT_ID = UUID.fromString("aaaaaaaa-1111-2222-3333-bbbbbbbbbbbb");
    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");
    private static final UUID PRODUCT_ID = UUID.fromString("99998888-7777-6666-5555-444433332222");
    private static final UUID PURCHASE_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID SALE_ID = UUID.fromString("abcdefab-cdef-abcd-efab-cdefabcdefab");
    private static final UUID USER_ID = UUID.fromString("77777777-6666-5555-4444-333333333333");
    private static final Instant NOW = Instant.parse("2026-05-11T23:55:00Z");

    @Test
    void purchaseInIncreasesStockAndRecordsTraceableMovement() {
        InMemoryStockBalanceRepository stockRepository = new InMemoryStockBalanceRepository();
        CapturingMovementRepository movementRepository = new CapturingMovementRepository();
        RegisterInventoryMovementService service = service(stockRepository, movementRepository);

        InventoryMovementResult result = service.register(command(
                InventoryMovementType.PURCHASE_IN,
                InventorySourceDocumentType.PURCHASE,
                PURCHASE_ID,
                "15.0000"));

        assertThat(result.previousStock()).isEqualByComparingTo("0");
        assertThat(result.resultingStock()).isEqualByComparingTo("15.0000");
        assertThat(stockRepository.savedBalance().currentStock()).isEqualByComparingTo("15.0000");
        assertThat(movementRepository.savedMovement().sourceDocumentType())
                .isEqualTo(InventorySourceDocumentType.PURCHASE);
        assertThat(movementRepository.savedMovement().sourceDocumentId()).isEqualTo(PURCHASE_ID);
    }

    @Test
    void saleOutDecreasesStockAndRecordsTraceableMovement() {
        InMemoryStockBalanceRepository stockRepository = new InMemoryStockBalanceRepository();
        stockRepository.save(StockBalance.restore(
                COMPANY_ID,
                PRODUCT_ID,
                new BigDecimal("20.0000"),
                BigDecimal.ZERO));
        CapturingMovementRepository movementRepository = new CapturingMovementRepository();
        RegisterInventoryMovementService service = service(stockRepository, movementRepository);

        InventoryMovementResult result = service.register(command(
                InventoryMovementType.SALE_OUT,
                InventorySourceDocumentType.SALE,
                SALE_ID,
                "4.5000"));

        assertThat(result.previousStock()).isEqualByComparingTo("20.0000");
        assertThat(result.resultingStock()).isEqualByComparingTo("15.5000");
        assertThat(result.movementType()).isEqualTo(InventoryMovementType.SALE_OUT);
        assertThat(result.companyId()).isEqualTo(COMPANY_ID);
        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.createdBy()).isEqualTo(USER_ID);
        assertThat(result.movementAt()).isEqualTo(NOW);
    }

    @Test
    void movementKeepsRequiredTraceabilityFields() {
        InMemoryStockBalanceRepository stockRepository = new InMemoryStockBalanceRepository();
        CapturingMovementRepository movementRepository = new CapturingMovementRepository();
        RegisterInventoryMovementService service = service(stockRepository, movementRepository);

        service.register(command(
                InventoryMovementType.PURCHASE_IN,
                InventorySourceDocumentType.PURCHASE,
                PURCHASE_ID,
                "2.0000"));

        InventoryMovement movement = movementRepository.savedMovement();
        assertThat(movement.id()).isEqualTo(MOVEMENT_ID);
        assertThat(movement.companyId()).isEqualTo(COMPANY_ID);
        assertThat(movement.productId()).isEqualTo(PRODUCT_ID);
        assertThat(movement.quantity()).isEqualByComparingTo("2.0000");
        assertThat(movement.createdBy()).isEqualTo(USER_ID);
        assertThat(movement.movementAt()).isEqualTo(NOW);
    }

    @Test
    void saleOutRejectsResultingNegativeStock() {
        RegisterInventoryMovementService service = service(
                new InMemoryStockBalanceRepository(),
                new CapturingMovementRepository());

        assertThatThrownBy(() -> service.register(command(
                InventoryMovementType.SALE_OUT,
                InventorySourceDocumentType.SALE,
                SALE_ID,
                "1.0000")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("stock is insufficient for movement");
    }

    private static RegisterInventoryMovementService service(
            StockBalanceRepositoryPort stockRepository,
            InventoryMovementRepositoryPort movementRepository) {
        return new RegisterInventoryMovementService(
                stockRepository,
                movementRepository,
                () -> MOVEMENT_ID,
                () -> NOW);
    }

    private static RegisterInventoryMovementCommand command(
            InventoryMovementType movementType,
            InventorySourceDocumentType sourceDocumentType,
            UUID sourceDocumentId,
            String quantity) {
        return new RegisterInventoryMovementCommand(
                COMPANY_ID,
                PRODUCT_ID,
                movementType,
                new BigDecimal(quantity),
                sourceDocumentType,
                sourceDocumentId,
                USER_ID);
    }

    private static final class InMemoryStockBalanceRepository implements StockBalanceRepositoryPort {

        private final Map<String, StockBalance> balances = new HashMap<>();
        private StockBalance savedBalance;

        @Override
        public Optional<StockBalance> findByCompanyIdAndProductId(UUID companyId, UUID productId) {
            return Optional.ofNullable(balances.get(key(companyId, productId)));
        }

        @Override
        public StockBalance save(StockBalance stockBalance) {
            savedBalance = stockBalance;
            balances.put(key(stockBalance.companyId(), stockBalance.productId()), stockBalance);
            return stockBalance;
        }

        private StockBalance savedBalance() {
            return savedBalance;
        }

        private static String key(UUID companyId, UUID productId) {
            return companyId + ":" + productId;
        }
    }

    private static final class CapturingMovementRepository implements InventoryMovementRepositoryPort {

        private InventoryMovement savedMovement;

        @Override
        public InventoryMovement save(InventoryMovement movement) {
            savedMovement = movement;
            return movement;
        }

        private InventoryMovement savedMovement() {
            return savedMovement;
        }
    }
}
