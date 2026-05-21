package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ServiceSupplyReferenceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryItemType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Product;

@ExtendWith(MockitoExtension.class)
class ServiceSupplyReferenceManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SERVICE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SUPPLY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID REFERENCE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");

    @Mock
    private ServiceSupplyReferenceRepositoryPort referenceRepository;

    @Mock
    private ProductRepositoryPort productRepository;

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

    private ServiceSupplyReferenceManagementService service() {
        return new ServiceSupplyReferenceManagementService(referenceRepository, productRepository, idGenerator, clock);
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
}
