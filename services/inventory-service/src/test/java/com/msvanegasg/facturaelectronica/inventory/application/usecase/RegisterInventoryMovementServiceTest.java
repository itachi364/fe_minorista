package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementQuery;
import com.msvanegasg.facturaelectronica.inventory.application.dto.RegisterInventoryMovementCommand;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.InventoryMovementRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.StockBalanceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovement;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryItemType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Product;
import com.msvanegasg.facturaelectronica.inventory.domain.model.StockBalance;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.DomainEventPublisherPort;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

@ExtendWith(MockitoExtension.class)
class RegisterInventoryMovementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID MOVEMENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SOURCE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID EVENT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    @Mock
    private ProductRepositoryPort productRepository;

    @Mock
    private StockBalanceRepositoryPort stockBalanceRepository;

    @Mock
    private InventoryMovementRepositoryPort movementRepository;

    @Mock
    private IdGeneratorPort idGenerator;

    @Mock
    private ClockPort clock;

    @Mock
    private DomainEventPublisherPort eventPublisher;

    @Test
    void registersMovementAndUpdatesStock() {
        RegisterInventoryMovementService service = service();
        when(movementRepository.findIdempotent(COMPANY_ID, InventorySourceDocumentType.PURCHASE, SOURCE_ID,
                InventoryMovementType.PURCHASE_IN, "purchase-1")).thenReturn(Optional.empty());
        when(productRepository.findByCompanyIdAndId(COMPANY_ID, PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(clock.now()).thenReturn(NOW);
        when(idGenerator.newId()).thenReturn(MOVEMENT_ID);
        when(stockBalanceRepository.findByCompanyIdAndProductId(COMPANY_ID, PRODUCT_ID)).thenReturn(Optional.empty());
        when(movementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(stockBalanceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.register(command(InventoryMovementType.PURCHASE_IN, "purchase-1"));

        assertThat(result.resultingStock()).isEqualByComparingTo("3.00");
        ArgumentCaptor<StockBalance> balanceCaptor = ArgumentCaptor.forClass(StockBalance.class);
        verify(stockBalanceRepository).save(balanceCaptor.capture());
        assertThat(balanceCaptor.getValue().currentStock()).isEqualByComparingTo("3.00");
    }


    @Test
    void publishesInventoryMovementRegisteredEventForNewMovement() {
        RegisterInventoryMovementService service = service(eventPublisher);
        when(movementRepository.findIdempotent(COMPANY_ID, InventorySourceDocumentType.PURCHASE, SOURCE_ID,
                InventoryMovementType.PURCHASE_IN, "purchase-1")).thenReturn(Optional.empty());
        when(productRepository.findByCompanyIdAndId(COMPANY_ID, PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(clock.now()).thenReturn(NOW);
        when(idGenerator.newId()).thenReturn(MOVEMENT_ID, EVENT_ID);
        when(stockBalanceRepository.findByCompanyIdAndProductId(COMPANY_ID, PRODUCT_ID)).thenReturn(Optional.empty());
        when(movementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(stockBalanceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.register(command(InventoryMovementType.PURCHASE_IN, "purchase-1"));

        ArgumentCaptor<DomainEventEnvelope> eventCaptor = ArgumentCaptor.forClass(DomainEventEnvelope.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        DomainEventEnvelope event = eventCaptor.getValue();
        assertThat(event.eventId()).isEqualTo(EVENT_ID);
        assertThat(event.eventType()).isEqualTo(EventTypes.INVENTORY_MOVEMENT_REGISTERED);
        assertThat(event.companyId()).isEqualTo(COMPANY_ID);
        assertThat(event.aggregateType()).isEqualTo("InventoryMovement");
        assertThat(event.aggregateId()).isEqualTo(MOVEMENT_ID);
        assertThat(event.idempotencyKey()).isEqualTo("purchase-1:inventory-movement-registered");
        assertThat(event.payload()).containsEntry("movementType", "PURCHASE_IN");
        assertThat(event.payload()).containsEntry("sourceDocumentId", SOURCE_ID.toString());
    }

    @Test
    void returnsExistingMovementWhenCommandIsIdempotent() {
        RegisterInventoryMovementService service = service();
        InventoryMovement existing = InventoryMovement.from(MOVEMENT_ID,
                StockBalance.empty(COMPANY_ID, PRODUCT_ID, NOW),
                new StockBalance(COMPANY_ID, PRODUCT_ID, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN, NOW),
                InventoryMovementType.PURCHASE_IN, BigDecimal.ONE, BigDecimal.TEN,
                InventorySourceDocumentType.PURCHASE, SOURCE_ID, "purchase-1", null, null, NOW);
        when(movementRepository.findIdempotent(COMPANY_ID, InventorySourceDocumentType.PURCHASE, SOURCE_ID,
                InventoryMovementType.PURCHASE_IN, "purchase-1")).thenReturn(Optional.of(existing));

        var result = service.register(command(InventoryMovementType.PURCHASE_IN, "purchase-1"));

        assertThat(result.id()).isEqualTo(MOVEMENT_ID);
        verify(stockBalanceRepository, never()).save(any());
        verify(movementRepository, never()).save(any());
    }

    @Test
    void rejectsSaleOutWhenStockIsInsufficient() {
        RegisterInventoryMovementService service = service();
        when(movementRepository.findIdempotent(COMPANY_ID, InventorySourceDocumentType.PURCHASE, SOURCE_ID,
                InventoryMovementType.SALE_OUT, "sale-1")).thenReturn(Optional.empty());
        when(productRepository.findByCompanyIdAndId(COMPANY_ID, PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(clock.now()).thenReturn(NOW);
        when(stockBalanceRepository.findByCompanyIdAndProductId(COMPANY_ID, PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.register(command(InventoryMovementType.SALE_OUT, "sale-1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("stock is insufficient for movement");
    }

    @Test
    void registersConsumptionOutWithRequiredReasonAndDecreasesStock() {
        RegisterInventoryMovementService service = service();
        when(movementRepository.findIdempotent(COMPANY_ID, InventorySourceDocumentType.MANUAL_SUPPLY_CONSUMPTION,
                SOURCE_ID, InventoryMovementType.CONSUMPTION_OUT, "consumption-1")).thenReturn(Optional.empty());
        when(productRepository.findByCompanyIdAndId(COMPANY_ID, PRODUCT_ID)).thenReturn(Optional.of(supply()));
        when(clock.now()).thenReturn(NOW);
        when(idGenerator.newId()).thenReturn(MOVEMENT_ID);
        when(stockBalanceRepository.findByCompanyIdAndProductId(COMPANY_ID, PRODUCT_ID))
                .thenReturn(Optional.of(new StockBalance(COMPANY_ID, PRODUCT_ID, new BigDecimal("8.00"),
                        BigDecimal.ZERO, new BigDecimal("1000.00"), NOW)));
        when(movementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(stockBalanceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.register(new RegisterInventoryMovementCommand(COMPANY_ID, PRODUCT_ID,
                InventoryMovementType.CONSUMPTION_OUT, new BigDecimal("3.00"), new BigDecimal("1000.00"),
                InventorySourceDocumentType.MANUAL_SUPPLY_CONSUMPTION, SOURCE_ID, "consumption-1",
                "Consumo operativo de insumo usado en servicios", null));

        assertThat(result.previousStock()).isEqualByComparingTo("8.00");
        assertThat(result.resultingStock()).isEqualByComparingTo("5.00");
        assertThat(result.reason()).isEqualTo("Consumo operativo de insumo usado en servicios");
    }

    @Test
    void registersWasteOutWithRequiredReasonAndDecreasesStock() {
        RegisterInventoryMovementService service = service();
        when(movementRepository.findIdempotent(COMPANY_ID, InventorySourceDocumentType.MANUAL_SUPPLY_WASTE,
                SOURCE_ID, InventoryMovementType.WASTE_OUT, "waste-1")).thenReturn(Optional.empty());
        when(productRepository.findByCompanyIdAndId(COMPANY_ID, PRODUCT_ID)).thenReturn(Optional.of(supply()));
        when(clock.now()).thenReturn(NOW);
        when(idGenerator.newId()).thenReturn(MOVEMENT_ID);
        when(stockBalanceRepository.findByCompanyIdAndProductId(COMPANY_ID, PRODUCT_ID))
                .thenReturn(Optional.of(new StockBalance(COMPANY_ID, PRODUCT_ID, new BigDecimal("8.00"),
                        BigDecimal.ZERO, new BigDecimal("1000.00"), NOW)));
        when(movementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(stockBalanceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.register(new RegisterInventoryMovementCommand(COMPANY_ID, PRODUCT_ID,
                InventoryMovementType.WASTE_OUT, new BigDecimal("1.00"), new BigDecimal("1000.00"),
                InventorySourceDocumentType.MANUAL_SUPPLY_WASTE, SOURCE_ID, "waste-1",
                "Desperdicio por producto terminado", null));

        assertThat(result.previousStock()).isEqualByComparingTo("8.00");
        assertThat(result.resultingStock()).isEqualByComparingTo("7.00");
        assertThat(result.reason()).isEqualTo("Desperdicio por producto terminado");
    }


    @Test
    void findsKardexByDateRange() {
        RegisterInventoryMovementService service = service();
        InventoryMovement movement = InventoryMovement.from(MOVEMENT_ID,
                StockBalance.empty(COMPANY_ID, PRODUCT_ID, NOW),
                new StockBalance(COMPANY_ID, PRODUCT_ID, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN, NOW),
                InventoryMovementType.PURCHASE_IN, BigDecimal.ONE, BigDecimal.TEN,
                InventorySourceDocumentType.PURCHASE, SOURCE_ID, "purchase-1", null, null, NOW);
        when(movementRepository.findKardex(COMPANY_ID, PRODUCT_ID, LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31))).thenReturn(List.of(movement));

        var result = service.kardex(new InventoryMovementQuery(COMPANY_ID, PRODUCT_ID,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(MOVEMENT_ID);
    }

    @Test
    void rejectsConsumptionOutWithoutReason() {
        RegisterInventoryMovementService service = service();

        assertThatThrownBy(() -> service.register(new RegisterInventoryMovementCommand(COMPANY_ID, PRODUCT_ID,
                InventoryMovementType.CONSUMPTION_OUT, new BigDecimal("3.00"), new BigDecimal("1000.00"),
                InventorySourceDocumentType.MANUAL_SUPPLY_CONSUMPTION, SOURCE_ID, "consumption-1", " ", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("reason is required for CONSUMPTION_OUT");
    }

    private RegisterInventoryMovementService service() {
        return new RegisterInventoryMovementService(productRepository, stockBalanceRepository, movementRepository,
                idGenerator, clock);
    }

    private RegisterInventoryMovementService service(DomainEventPublisherPort publisher) {
        return new RegisterInventoryMovementService(productRepository, stockBalanceRepository, movementRepository,
                publisher, idGenerator, clock);
    }

    private static RegisterInventoryMovementCommand command(InventoryMovementType type, String idempotencyKey) {
        return new RegisterInventoryMovementCommand(COMPANY_ID, PRODUCT_ID, type, new BigDecimal("3.00"),
                new BigDecimal("1000.00"), InventorySourceDocumentType.PURCHASE, SOURCE_ID, idempotencyKey, null,
                null);
    }

    private static Product product() {
        return Product.create(PRODUCT_ID, COMPANY_ID, "SKU-1", null, "Cafe", null, InventoryItemType.PHYSICAL_GOOD,
                true, true, true, new BigDecimal("1500.00"), new BigDecimal("1000.00"), NOW);
    }

    private static Product supply() {
        return Product.create(PRODUCT_ID, COMPANY_ID, "SUP-1", null, "Esmalte", null, InventoryItemType.SUPPLY,
                false, true, true, BigDecimal.ZERO, new BigDecimal("1000.00"), NOW);
    }
}
