package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msvanegasg.facturaelectronica.inventory.application.dto.CreateProductCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.RegisterInventoryMovementCommand;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.RegisterInventoryMovementUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.StockBalanceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryItemType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Product;

@ExtendWith(MockitoExtension.class)
class ProductManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");

    @Mock
    private ProductRepositoryPort productRepository;

    @Mock
    private StockBalanceRepositoryPort stockBalanceRepository;

    @Mock
    private RegisterInventoryMovementUseCase movementUseCase;

    @Mock
    private IdGeneratorPort idGenerator;

    @Mock
    private ClockPort clock;

    @Test
    void createsServiceWithoutStockMovement() {
        ProductManagementService service = service();
        AtomicReference<Product> savedProduct = new AtomicReference<>();
        when(productRepository.existsByCompanyIdAndSku(COMPANY_ID, "SERV-1")).thenReturn(false);
        when(idGenerator.newId()).thenReturn(PRODUCT_ID);
        when(clock.now()).thenReturn(NOW);
        when(productRepository.save(any())).thenAnswer(invocation -> {
            savedProduct.set(invocation.getArgument(0));
            return savedProduct.get();
        });
        when(productRepository.findByCompanyIdAndId(COMPANY_ID, PRODUCT_ID))
                .thenAnswer(invocation -> Optional.of(savedProduct.get()));
        when(stockBalanceRepository.findByCompanyIdAndProductId(COMPANY_ID, PRODUCT_ID)).thenReturn(Optional.empty());

        var result = service.create(new CreateProductCommand(COMPANY_ID, "SERV-1", null, "Manicura", null,
                InventoryItemType.SERVICE, null, null, null, new BigDecimal("35000.00"), BigDecimal.ZERO, null,
                null, null));

        assertThat(result.itemType()).isEqualTo(InventoryItemType.SERVICE);
        assertThat(result.saleEnabled()).isTrue();
        assertThat(result.purchaseEnabled()).isFalse();
        assertThat(result.stockTracked()).isFalse();
        assertThat(result.currentStock()).isZero();
        verify(movementUseCase, never()).register(any(RegisterInventoryMovementCommand.class));
    }

    @Test
    void rejectsInitialStockForService() {
        ProductManagementService service = service();
        when(productRepository.existsByCompanyIdAndSku(COMPANY_ID, "SERV-1")).thenReturn(false);
        when(idGenerator.newId()).thenReturn(PRODUCT_ID);
        when(clock.now()).thenReturn(NOW);

        assertThatThrownBy(() -> service.create(new CreateProductCommand(COMPANY_ID, "SERV-1", null, "Manicura",
                null, InventoryItemType.SERVICE, null, null, null, new BigDecimal("35000.00"), BigDecimal.ZERO,
                BigDecimal.ONE, null, "service-1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("initial stock is only allowed for stock tracked items");
        verify(productRepository, never()).save(any());
    }

    @Test
    void serviceAvailabilityDoesNotRequireStockBalance() {
        ProductManagementService service = service();
        Product product = Product.create(PRODUCT_ID, COMPANY_ID, "SERV-1", null, "Manicura", null,
                InventoryItemType.SERVICE, true, false, false, new BigDecimal("35000.00"), BigDecimal.ZERO, NOW);
        when(productRepository.findByCompanyIdAndId(COMPANY_ID, PRODUCT_ID)).thenReturn(Optional.of(product));
        when(stockBalanceRepository.findByCompanyIdAndProductId(COMPANY_ID, PRODUCT_ID)).thenReturn(Optional.empty());

        var result = service.checkAvailability(COMPANY_ID, PRODUCT_ID, new BigDecimal("99.00"));

        assertThat(result.available()).isTrue();
        assertThat(result.currentStock()).isZero();
    }

    private ProductManagementService service() {
        return new ProductManagementService(productRepository, stockBalanceRepository, movementUseCase, idGenerator,
                clock);
    }
}
