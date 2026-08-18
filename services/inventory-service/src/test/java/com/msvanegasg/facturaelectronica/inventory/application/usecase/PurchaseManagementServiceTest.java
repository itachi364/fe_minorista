package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msvanegasg.facturaelectronica.inventory.application.dto.RegisterInventoryMovementCommand;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.RegisterInventoryMovementUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.PurchaseAccountingPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.PurchaseRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryItemType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PaymentCondition;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Product;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseLine;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseStatus;

@ExtendWith(MockitoExtension.class)
class PurchaseManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PURCHASE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PRODUCT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID LINE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID USER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final Instant NOW = Instant.parse("2026-08-18T10:00:00Z");

    @Mock
    private PurchaseRepositoryPort purchaseRepository;

    @Mock
    private ProductRepositoryPort productRepository;

    @Mock
    private RegisterInventoryMovementUseCase movementUseCase;

    @Mock
    private PurchaseAccountingPort purchaseAccountingPort;

    @Mock
    private IdGeneratorPort idGenerator;

    @Mock
    private ClockPort clock;

    @Test
    void confirmPurchaseRegistersStockMovementAndAppliesAccounting() {
        Purchase pending = pendingPurchase();
        when(purchaseRepository.findByCompanyIdAndId(COMPANY_ID, PURCHASE_ID)).thenReturn(Optional.of(pending));
        when(clock.now()).thenReturn(NOW);
        when(purchaseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service().confirm(COMPANY_ID, PURCHASE_ID, USER_ID);

        assertThat(result.status()).isEqualTo(PurchaseStatus.CONFIRMED);
        assertThat(result.confirmedAt()).isEqualTo(NOW);

        ArgumentCaptor<RegisterInventoryMovementCommand> movementCaptor =
                ArgumentCaptor.forClass(RegisterInventoryMovementCommand.class);
        verify(movementUseCase).register(movementCaptor.capture());
        RegisterInventoryMovementCommand movement = movementCaptor.getValue();
        assertThat(movement.companyId()).isEqualTo(COMPANY_ID);
        assertThat(movement.productId()).isEqualTo(PRODUCT_ID);
        assertThat(movement.movementType()).isEqualTo(InventoryMovementType.PURCHASE_IN);
        assertThat(movement.quantity()).isEqualByComparingTo("5.00");
        assertThat(movement.unitCost()).isEqualByComparingTo("9000.00");
        assertThat(movement.sourceDocumentType()).isEqualTo(InventorySourceDocumentType.PURCHASE);
        assertThat(movement.sourceDocumentId()).isEqualTo(PURCHASE_ID);
        assertThat(movement.idempotencyKey()).isEqualTo("purchase-1-" + LINE_ID);
        assertThat(movement.createdBy()).isEqualTo(USER_ID);

        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseAccountingPort).applyConfirmedPurchase(purchaseCaptor.capture(), any());
        assertThat(purchaseCaptor.getValue().status()).isEqualTo(PurchaseStatus.CONFIRMED);
    }

    @Test
    void confirmPurchaseIsIdempotentWhenAlreadyConfirmed() {
        Purchase confirmed = pendingPurchase().confirm(NOW);
        when(purchaseRepository.findByCompanyIdAndId(COMPANY_ID, PURCHASE_ID)).thenReturn(Optional.of(confirmed));

        var result = service().confirm(COMPANY_ID, PURCHASE_ID, USER_ID);

        assertThat(result.status()).isEqualTo(PurchaseStatus.CONFIRMED);
        verify(movementUseCase, never()).register(any());
        verify(purchaseRepository, never()).save(any());
        verify(purchaseAccountingPort, never()).applyConfirmedPurchase(any(), any());
    }

    private PurchaseManagementService service() {
        return new PurchaseManagementService(purchaseRepository, productRepository, movementUseCase,
                purchaseAccountingPort, idGenerator, clock);
    }

    private static Purchase pendingPurchase() {
        return Purchase.pending(PURCHASE_ID, COMPANY_ID, UUID.fromString("66666666-6666-6666-6666-666666666666"),
                new BigDecimal("45000.00"), new BigDecimal("8550.00"), new BigDecimal("53550.00"),
                PaymentCondition.CREDIT, LocalDate.of(2026, 12, 31), null, "purchase-1", NOW,
                List.of(new PurchaseLine(LINE_ID, PURCHASE_ID, PRODUCT_ID, new BigDecimal("5.00"),
                        new BigDecimal("9000.00"), new BigDecimal("45000.00"), new BigDecimal("8550.00"),
                        new BigDecimal("53550.00"))));
    }

    @SuppressWarnings("unused")
    private static Product product() {
        return Product.create(PRODUCT_ID, COMPANY_ID, "SKU-1", null, "Cafe", null,
                InventoryItemType.PHYSICAL_GOOD, true, true, true, new BigDecimal("15000.00"),
                new BigDecimal("9000.00"), NOW);
    }
}
