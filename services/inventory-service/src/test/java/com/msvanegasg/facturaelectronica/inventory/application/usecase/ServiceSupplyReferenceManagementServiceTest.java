package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msvanegasg.facturaelectronica.inventory.application.dto.CreateServiceSupplyReferenceCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ConfirmServiceSupplyConsumptionCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.RegisterInventoryMovementCommand;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.RegisterInventoryMovementUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ServiceSupplyReferenceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.StockBalanceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryItemType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Product;
import com.msvanegasg.facturaelectronica.inventory.domain.model.ServiceSupplyReference;
import com.msvanegasg.facturaelectronica.inventory.domain.model.StockBalance;

@ExtendWith(MockitoExtension.class)
class ServiceSupplyReferenceManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SERVICE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SUPPLY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID REFERENCE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID SALE_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID USER_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID MOVEMENT_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");

    @Mock
    private ServiceSupplyReferenceRepositoryPort referenceRepository;

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
    void createsServiceSupplyReferenceWithoutStockMovement() {
        ServiceSupplyReferenceManagementService service = service();
        when(productRepository.findByCompanyIdAndId(COMPANY_ID, SERVICE_ID)).thenReturn(Optional.of(serviceProduct()));
        when(productRepository.findByCompanyIdAndId(COMPANY_ID, SUPPLY_ID)).thenReturn(Optional.of(supplyProduct()));
        when(referenceRepository.existsByCompanyIdAndServiceProductIdAndSupplyProductId(COMPANY_ID, SERVICE_ID,
                SUPPLY_ID)).thenReturn(false);
        when(idGenerator.newId()).thenReturn(REFERENCE_ID);
        when(clock.now()).thenReturn(NOW);
        when(referenceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create(new CreateServiceSupplyReferenceCommand(COMPANY_ID, SERVICE_ID, SUPPLY_ID,
                "Esmalte sugerido"));

        assertThat(result.id()).isEqualTo(REFERENCE_ID);
        assertThat(result.serviceProductId()).isEqualTo(SERVICE_ID);
        assertThat(result.supplyProductId()).isEqualTo(SUPPLY_ID);
    }

    @Test
    void rejectsReferenceWhenMainProductIsNotService() {
        ServiceSupplyReferenceManagementService service = service();
        when(productRepository.findByCompanyIdAndId(COMPANY_ID, SERVICE_ID)).thenReturn(Optional.of(supplyProduct()));
        when(productRepository.findByCompanyIdAndId(COMPANY_ID, SUPPLY_ID)).thenReturn(Optional.of(supplyProduct()));

        assertThatThrownBy(() -> service.create(command()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("service product must have item type SERVICE");
    }

    @Test
    void listsReferencesOnlyForServiceProducts() {
        ServiceSupplyReferenceManagementService service = service();
        when(productRepository.findByCompanyIdAndId(COMPANY_ID, SERVICE_ID)).thenReturn(Optional.of(serviceProduct()));
        when(referenceRepository.findByCompanyIdAndServiceProductId(COMPANY_ID, SERVICE_ID)).thenReturn(List.of());

        assertThat(service.findByService(COMPANY_ID, SERVICE_ID)).isEmpty();
    }

    @Test
    void suggestsConsumptionsForServiceSupplyReferences() {
        ServiceSupplyReferenceManagementService service = service();
        when(productRepository.findByCompanyIdAndId(COMPANY_ID, SERVICE_ID)).thenReturn(Optional.of(serviceProduct()));
        when(referenceRepository.findByCompanyIdAndServiceProductId(COMPANY_ID, SERVICE_ID))
                .thenReturn(List.of(reference()));
        when(productRepository.findByCompanyIdAndId(COMPANY_ID, SUPPLY_ID)).thenReturn(Optional.of(supplyProduct()));
        when(stockBalanceRepository.findByCompanyIdAndProductId(COMPANY_ID, SUPPLY_ID))
                .thenReturn(Optional.of(new StockBalance(COMPANY_ID, SUPPLY_ID, new BigDecimal("10.00"),
                        BigDecimal.ZERO, new BigDecimal("1500.00"), NOW)));

        var result = service.suggestConsumptions(COMPANY_ID, SERVICE_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).supplyName()).isEqualTo("Esmalte");
        assertThat(result.get(0).currentStock()).isEqualByComparingTo("10.00");
        assertThat(result.get(0).unitCost()).isEqualByComparingTo("1500.00");
    }

    @Test
    void confirmsRealConsumptionForServiceSupply() {
        ServiceSupplyReferenceManagementService service = service();
        when(productRepository.findByCompanyIdAndId(COMPANY_ID, SERVICE_ID)).thenReturn(Optional.of(serviceProduct()));
        when(referenceRepository.findByCompanyIdAndServiceProductId(COMPANY_ID, SERVICE_ID))
                .thenReturn(List.of(reference()));
        when(productRepository.findByCompanyIdAndId(COMPANY_ID, SUPPLY_ID)).thenReturn(Optional.of(supplyProduct()));
        when(stockBalanceRepository.findByCompanyIdAndProductId(COMPANY_ID, SUPPLY_ID))
                .thenReturn(Optional.of(new StockBalance(COMPANY_ID, SUPPLY_ID, new BigDecimal("10.00"),
                        BigDecimal.ZERO, new BigDecimal("1500.00"), NOW)));
        when(movementUseCase.register(any())).thenReturn(movementResult());

        var result = service.confirmConsumption(new ConfirmServiceSupplyConsumptionCommand(COMPANY_ID, SERVICE_ID,
                SALE_ID, "Consumo real de insumos por manicura facturada", USER_ID, "service-consumption-1",
                List.of(new ConfirmServiceSupplyConsumptionCommand.Line(SUPPLY_ID, new BigDecimal("0.25")))));

        assertThat(result.sourceDocumentId()).isEqualTo(SALE_ID);
        assertThat(result.movements()).hasSize(1);
        verify(movementUseCase).register(new RegisterInventoryMovementCommand(COMPANY_ID, SUPPLY_ID,
                InventoryMovementType.CONSUMPTION_OUT, new BigDecimal("0.25"), new BigDecimal("1500.00"),
                InventorySourceDocumentType.MANUAL_SUPPLY_CONSUMPTION, SALE_ID,
                "service-consumption-1-service-supply-" + SUPPLY_ID,
                "Consumo real de insumos por manicura facturada", USER_ID));
    }

    private ServiceSupplyReferenceManagementService service() {
        return new ServiceSupplyReferenceManagementService(referenceRepository, productRepository,
                stockBalanceRepository, movementUseCase, idGenerator, clock);
    }

    private static CreateServiceSupplyReferenceCommand command() {
        return new CreateServiceSupplyReferenceCommand(COMPANY_ID, SERVICE_ID, SUPPLY_ID, "Esmalte sugerido");
    }

    private static Product serviceProduct() {
        return Product.create(SERVICE_ID, COMPANY_ID, "SERV-1", null, "Manicura", null, InventoryItemType.SERVICE,
                true, false, false, new BigDecimal("35000.00"), BigDecimal.ZERO, NOW);
    }

    private static Product supplyProduct() {
        return Product.create(SUPPLY_ID, COMPANY_ID, "SUP-1", null, "Esmalte", null, InventoryItemType.SUPPLY,
                false, true, true, BigDecimal.ZERO, new BigDecimal("12000.00"), NOW);
    }

    private static ServiceSupplyReference reference() {
        return ServiceSupplyReference.create(REFERENCE_ID, COMPANY_ID, SERVICE_ID, SUPPLY_ID, "Esmalte sugerido",
                NOW);
    }

    private static InventoryMovementResult movementResult() {
        return new InventoryMovementResult(MOVEMENT_ID, COMPANY_ID, SUPPLY_ID, InventoryMovementType.CONSUMPTION_OUT,
                new BigDecimal("0.25"), new BigDecimal("1500.00"), new BigDecimal("10.00"),
                new BigDecimal("9.75"), InventorySourceDocumentType.MANUAL_SUPPLY_CONSUMPTION, SALE_ID,
                "service-consumption-1-service-supply-" + SUPPLY_ID, USER_ID,
                "Consumo real de insumos por manicura facturada", NOW);
    }
}
