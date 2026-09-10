package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateFiscalLegalEventCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateFiscalLegalSourceCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalLegalSourceRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalSource;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalSourceEvent;

class FiscalLegalSourceManagementServiceTest {
    private static final UUID SOURCE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID EVENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-10T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void resolvesSuspensionAndCreatesHighSeverityWarning() {
        InMemoryRepository repository = repositoryWithDecree();
        FiscalLegalSourceManagementService service = service(repository);

        assertThat(service.timelines(LocalDate.of(2026, 5, 7)).get(0).status())
                .isEqualTo(FiscalLegalEventType.EFFECTIVE);
        assertThat(service.timelines(LocalDate.of(2026, 5, 8)).get(0).status())
                .isEqualTo(FiscalLegalEventType.SUSPENDED);
        assertThat(service.warnings(LocalDate.of(2026, 5, 8)))
                .singleElement().satisfies(warning -> {
                    assertThat(warning.severity()).isEqualTo("HIGH");
                    assertThat(warning.code()).isEqualTo("LEGAL_SOURCE_SUSPENDED");
                });
    }

    @Test
    void resolvesARegisteredReactivationWithoutChangingPastSuspension() {
        InMemoryRepository repository = repositoryWithDecree();
        repository.events.add(new FiscalLegalSourceEvent(UUID.randomUUID(), SOURCE_ID,
                FiscalLegalEventType.REACTIVATED, LocalDate.of(2026, 10, 1), null, "Providencia ejecutoriada",
                "https://dian.gov.co/reactivacion", null, Instant.now(CLOCK), USER_ID));
        FiscalLegalSourceManagementService service = service(repository);

        assertThat(service.timelines(LocalDate.of(2026, 9, 30)).get(0).status())
                .isEqualTo(FiscalLegalEventType.SUSPENDED);
        assertThat(service.timelines(LocalDate.of(2026, 10, 1)).get(0).status())
                .isEqualTo(FiscalLegalEventType.REACTIVATED);
    }

    @Test
    void createsSourceAndEventWithAuditIdentity() {
        InMemoryRepository repository = new InMemoryRepository();
        FiscalLegalSourceManagementService service = service(repository);

        FiscalLegalSource source = service.create(new CreateFiscalLegalSourceCommand("  bog-ica-2026 ",
                "Estatuto tributario Bogota", "Bogota D.C.", "https://bogota.gov.co/norma",
                LocalDate.of(2025, 12, 1), LocalDate.of(2026, 12, 1), USER_ID));
        FiscalLegalSourceEvent event = service.addEvent(new CreateFiscalLegalEventCommand(source.id(),
                FiscalLegalEventType.EFFECTIVE, LocalDate.of(2026, 1, 1), null, "Acuerdo 1",
                "https://bogota.gov.co/acuerdo", "Vigente", USER_ID));

        assertThat(source.code()).isEqualTo("BOG-ICA-2026");
        assertThat(source.createdBy()).isEqualTo(USER_ID);
        assertThat(event.createdBy()).isEqualTo(USER_ID);
        assertThat(service.timelines(LocalDate.of(2026, 1, 1))).singleElement();
    }

    @Test
    void rejectsInvalidSourcesAndEvents() {
        InMemoryRepository repository = repositoryWithDecree();
        FiscalLegalSourceManagementService service = service(repository);

        assertThatThrownBy(() -> service.create(new CreateFiscalLegalSourceCommand("CO-DECRETO-572-2025",
                "Duplicada", "DIAN", "https://dian.gov.co", null, null, USER_ID)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> service.create(new CreateFiscalLegalSourceCommand("NEW", "Nueva", "DIAN",
                "http://dian.gov.co", null, null, USER_ID)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.addEvent(new CreateFiscalLegalEventCommand(UUID.randomUUID(),
                FiscalLegalEventType.EFFECTIVE, LocalDate.now(), null, "Referencia", "https://dian.gov.co", null,
                USER_ID))).isInstanceOf(IllegalStateException.class);
    }

    private static FiscalLegalSourceManagementService service(InMemoryRepository repository) {
        List<UUID> ids = new ArrayList<>(List.of(SOURCE_ID, EVENT_ID));
        return new FiscalLegalSourceManagementService(repository, () -> ids.remove(0), CLOCK);
    }

    private static InMemoryRepository repositoryWithDecree() {
        InMemoryRepository repository = new InMemoryRepository();
        repository.sources.add(new FiscalLegalSource(SOURCE_ID, "CO-DECRETO-572-2025", "Decreto 572", "MHCP",
                "https://dian.gov.co/decreto", LocalDate.of(2025, 5, 28), LocalDate.of(2026, 11, 8), true,
                Instant.now(CLOCK), USER_ID));
        repository.events.add(new FiscalLegalSourceEvent(UUID.randomUUID(), SOURCE_ID, FiscalLegalEventType.EFFECTIVE,
                LocalDate.of(2025, 6, 1), LocalDate.of(2026, 5, 7), "Decreto", "https://dian.gov.co/decreto",
                null, Instant.now(CLOCK), USER_ID));
        repository.events.add(new FiscalLegalSourceEvent(UUID.randomUUID(), SOURCE_ID, FiscalLegalEventType.SUSPENDED,
                LocalDate.of(2026, 5, 8), null, "Auto", "https://dian.gov.co/auto", null, Instant.now(CLOCK), USER_ID));
        return repository;
    }

    private static final class InMemoryRepository implements FiscalLegalSourceRepositoryPort {
        private final List<FiscalLegalSource> sources = new ArrayList<>();
        private final List<FiscalLegalSourceEvent> events = new ArrayList<>();

        @Override public List<FiscalLegalSource> findAll() { return List.copyOf(sources); }
        @Override public Optional<FiscalLegalSource> findById(UUID sourceId) {
            return sources.stream().filter(source -> source.id().equals(sourceId)).findFirst();
        }
        @Override public List<FiscalLegalSourceEvent> findEvents(UUID sourceId) {
            return events.stream().filter(event -> event.sourceId().equals(sourceId)).toList();
        }
        @Override public FiscalLegalSource save(FiscalLegalSource source) { sources.add(source); return source; }
        @Override public FiscalLegalSourceEvent saveEvent(FiscalLegalSourceEvent event) {
            events.add(event); return event;
        }
    }
}
