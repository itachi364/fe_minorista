package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovement;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository.InventoryMovementJpaRepository;

@ExtendWith(MockitoExtension.class)
class InventoryMovementPersistenceAdapterTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID MOVEMENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SOURCE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");

    @Mock
    private InventoryMovementJpaRepository repository;

    @Test
    void savesAndRestoresManualSupplyMovementReason() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        InventoryMovementPersistenceAdapter adapter = new InventoryMovementPersistenceAdapter(repository);

        InventoryMovement saved = adapter.save(consumptionMovement());

        assertThat(saved.movementType()).isEqualTo(InventoryMovementType.CONSUMPTION_OUT);
        assertThat(saved.sourceDocumentType()).isEqualTo(InventorySourceDocumentType.MANUAL_SUPPLY_CONSUMPTION);
        assertThat(saved.reason()).isEqualTo("Consumo operativo de insumo usado en servicios");
        assertThat(saved.resultingStock()).isEqualByComparingTo("5.00");
    }

    private static InventoryMovement consumptionMovement() {
        return new InventoryMovement(MOVEMENT_ID, COMPANY_ID, PRODUCT_ID, InventoryMovementType.CONSUMPTION_OUT,
                new BigDecimal("3.00"), new BigDecimal("1000.00"), new BigDecimal("8.00"), new BigDecimal("5.00"),
                InventorySourceDocumentType.MANUAL_SUPPLY_CONSUMPTION, SOURCE_ID, "consumption-1",
                "Consumo operativo de insumo usado en servicios", null, NOW);
    }
}
