package com.msvanegasg.facturaelectronica.audit.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.audit.application.dto.AuditEventQuery;
import com.msvanegasg.facturaelectronica.audit.application.dto.RegisterAuditEventCommand;
import com.msvanegasg.facturaelectronica.audit.application.port.out.AuditEventRepositoryPort;
import com.msvanegasg.facturaelectronica.audit.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.audit.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.audit.domain.model.AuditEvent;
import com.msvanegasg.facturaelectronica.audit.domain.model.AuditResult;

class AuditEventServiceTest {

    private static final UUID EVENT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");

    private final InMemoryAuditRepository repository = new InMemoryAuditRepository();
    private final IdGeneratorPort idGenerator = () -> EVENT_ID;
    private final ClockPort clock = () -> NOW;

    @Test
    void registersAuditEventWithSafeDetail() {
        RegisterAuditEventService service = new RegisterAuditEventService(repository, idGenerator, clock);

        var result = service.register(new RegisterAuditEventCommand(COMPANY_ID, USER_ID, "ELECTRONIC_DOCUMENT",
                "SALE", "sale-1", "VALIDATED", AuditResult.SUCCESS, "{\"status\":\"ACCEPTED\"}"));

        assertThat(result.id()).isEqualTo(EVENT_ID);
        assertThat(result.companyId()).isEqualTo(COMPANY_ID);
        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.eventType()).isEqualTo("ELECTRONIC_DOCUMENT");
        assertThat(result.detail()).isEqualTo("{\"status\":\"ACCEPTED\"}");
        assertThat(result.occurredAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsMissingCompany() {
        RegisterAuditEventService service = new RegisterAuditEventService(repository, idGenerator, clock);

        assertThatThrownBy(() -> service.register(new RegisterAuditEventCommand(null, USER_ID,
                "ELECTRONIC_DOCUMENT", "SALE", "sale-1", "VALIDATED", AuditResult.SUCCESS, null)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("companyId");
    }

    @Test
    void queriesEventsByCompanyAndResource() {
        RegisterAuditEventService registerService = new RegisterAuditEventService(repository, idGenerator, clock);
        QueryAuditEventsService queryService = new QueryAuditEventsService(repository);
        registerService.register(new RegisterAuditEventCommand(COMPANY_ID, USER_ID, "ELECTRONIC_DOCUMENT",
                "SALE", "sale-1", "VALIDATED", AuditResult.SUCCESS, null));

        var events = queryService.find(new AuditEventQuery(COMPANY_ID, "SALE", "sale-1", null, null, null));

        assertThat(events).hasSize(1);
        assertThat(events.get(0).resourceId()).isEqualTo("sale-1");
    }

    @Test
    void listsResourceTypesByCompany() {
        RegisterAuditEventService registerService = new RegisterAuditEventService(repository, idGenerator, clock);
        QueryAuditEventsService queryService = new QueryAuditEventsService(repository);
        registerService.register(new RegisterAuditEventCommand(COMPANY_ID, USER_ID, "ELECTRONIC_DOCUMENT",
                "SALE", "sale-1", "VALIDATED", AuditResult.SUCCESS, null));

        assertThat(queryService.resourceTypes(COMPANY_ID)).containsExactly("SALE");
    }

    @Test
    void rejectsInvertedDateRange() {
        QueryAuditEventsService queryService = new QueryAuditEventsService(repository);

        assertThatThrownBy(() -> queryService.find(new AuditEventQuery(COMPANY_ID, null, null,
                Instant.parse("2026-05-21T00:00:00Z"), Instant.parse("2026-05-20T00:00:00Z"), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("from");
    }

    private static final class InMemoryAuditRepository implements AuditEventRepositoryPort {

        private final List<AuditEvent> events = new ArrayList<>();

        @Override
        public AuditEvent save(AuditEvent event) {
            events.add(event);
            return event;
        }

        @Override
        public List<AuditEvent> find(AuditEventQuery query) {
            return events.stream()
                    .filter(event -> event.companyId().equals(query.companyId()))
                    .filter(event -> query.resourceType() == null || event.resourceType().equals(query.resourceType()))
                    .filter(event -> query.resourceId() == null || event.resourceId().equals(query.resourceId()))
                    .toList();
        }

        @Override
        public List<String> resourceTypes(UUID companyId) {
            return events.stream()
                    .filter(event -> event.companyId().equals(companyId))
                    .map(AuditEvent::resourceType)
                    .distinct()
                    .sorted()
                    .toList();
        }
    }
}
