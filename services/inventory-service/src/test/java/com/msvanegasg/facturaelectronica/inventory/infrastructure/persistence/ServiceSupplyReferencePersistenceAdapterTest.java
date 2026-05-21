package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msvanegasg.facturaelectronica.inventory.domain.model.ServiceSupplyReference;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.ServiceSupplyReferenceJpaEntity;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository.ServiceSupplyReferenceJpaRepository;

@ExtendWith(MockitoExtension.class)
class ServiceSupplyReferencePersistenceAdapterTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SERVICE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SUPPLY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID REFERENCE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");

    @Mock
    private ServiceSupplyReferenceJpaRepository repository;

    @Test
    void savesAndRestoresReference() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ServiceSupplyReferencePersistenceAdapter adapter = new ServiceSupplyReferencePersistenceAdapter(repository);

        ServiceSupplyReference saved = adapter.save(reference());

        assertThat(saved.id()).isEqualTo(REFERENCE_ID);
        assertThat(saved.serviceProductId()).isEqualTo(SERVICE_ID);
        assertThat(saved.supplyProductId()).isEqualTo(SUPPLY_ID);
    }

    @Test
    void listsActiveReferencesByService() {
        when(repository.findByCompanyIdAndServiceProductIdAndActiveTrue(COMPANY_ID, SERVICE_ID))
                .thenReturn(List.of(entity()));
        ServiceSupplyReferencePersistenceAdapter adapter = new ServiceSupplyReferencePersistenceAdapter(repository);

        List<ServiceSupplyReference> result = adapter.findByCompanyIdAndServiceProductId(COMPANY_ID, SERVICE_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).notes()).isEqualTo("Esmalte sugerido");
    }

    private static ServiceSupplyReference reference() {
        return ServiceSupplyReference.create(REFERENCE_ID, COMPANY_ID, SERVICE_ID, SUPPLY_ID, "Esmalte sugerido",
                NOW);
    }

    private static ServiceSupplyReferenceJpaEntity entity() {
        ServiceSupplyReferenceJpaEntity entity = new ServiceSupplyReferenceJpaEntity();
        entity.setId(REFERENCE_ID);
        entity.setCompanyId(COMPANY_ID);
        entity.setServiceProductId(SERVICE_ID);
        entity.setSupplyProductId(SUPPLY_ID);
        entity.setNotes("Esmalte sugerido");
        entity.setActive(true);
        entity.setCreatedAt(NOW);
        return entity;
    }
}
