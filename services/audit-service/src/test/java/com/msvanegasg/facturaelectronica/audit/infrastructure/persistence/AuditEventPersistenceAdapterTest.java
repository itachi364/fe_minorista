package com.msvanegasg.facturaelectronica.audit.infrastructure.persistence;

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

import com.msvanegasg.facturaelectronica.audit.application.dto.AuditEventQuery;
import com.msvanegasg.facturaelectronica.audit.domain.model.AuditEvent;
import com.msvanegasg.facturaelectronica.audit.domain.model.AuditResult;
import com.msvanegasg.facturaelectronica.audit.infrastructure.persistence.entity.AuditEventJpaEntity;
import com.msvanegasg.facturaelectronica.audit.infrastructure.persistence.repository.AuditEventJpaRepository;

@ExtendWith(MockitoExtension.class)
class AuditEventPersistenceAdapterTest {

    private static final UUID EVENT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");

    @Mock
    private AuditEventJpaRepository repository;

    @Test
    void savesAndRestoresAuditEvent() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        AuditEventPersistenceAdapter adapter = new AuditEventPersistenceAdapter(repository);

        AuditEvent saved = adapter.save(event());

        assertThat(saved.id()).isEqualTo(EVENT_ID);
        assertThat(saved.companyId()).isEqualTo(COMPANY_ID);
        assertThat(saved.detail()).isEqualTo("{\"status\":\"ACCEPTED\"}");
    }

    @Test
    void queriesByFilters() {
        when(repository.findByCompanyIdOrderByOccurredAtDesc(COMPANY_ID)).thenReturn(List.of(entity()));
        AuditEventPersistenceAdapter adapter = new AuditEventPersistenceAdapter(repository);

        List<AuditEvent> events = adapter.find(new AuditEventQuery(COMPANY_ID, " SALE ", "sale-1", null, null,
                USER_ID));

        assertThat(events).hasSize(1);
        assertThat(events.get(0).resourceType()).isEqualTo("SALE");
    }

    @Test
    void listsDistinctResourceTypes() {
        when(repository.findDistinctResourceTypesByCompanyId(COMPANY_ID)).thenReturn(List.of("CATALOG", "SALE"));
        AuditEventPersistenceAdapter adapter = new AuditEventPersistenceAdapter(repository);

        assertThat(adapter.resourceTypes(COMPANY_ID)).containsExactly("CATALOG", "SALE");
    }

    private static AuditEvent event() {
        return AuditEvent.register(EVENT_ID, COMPANY_ID, USER_ID, "ELECTRONIC_DOCUMENT", "SALE", "sale-1",
                "VALIDATED", AuditResult.SUCCESS, "{\"status\":\"ACCEPTED\"}", NOW);
    }

    private static AuditEventJpaEntity entity() {
        AuditEventJpaEntity entity = new AuditEventJpaEntity();
        entity.setId(EVENT_ID);
        entity.setCompanyId(COMPANY_ID);
        entity.setUserId(USER_ID);
        entity.setEventType("ELECTRONIC_DOCUMENT");
        entity.setResourceType("SALE");
        entity.setResourceId("sale-1");
        entity.setAction("VALIDATED");
        entity.setResult(AuditResult.SUCCESS);
        entity.setDetail("{\"status\":\"ACCEPTED\"}");
        entity.setOccurredAt(NOW);
        return entity;
    }
}
