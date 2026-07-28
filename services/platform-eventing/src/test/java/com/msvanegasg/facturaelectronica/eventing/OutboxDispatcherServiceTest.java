package com.msvanegasg.facturaelectronica.eventing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class OutboxDispatcherServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-21T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void dispatchesEventsAndMarksThemPublished() {
        OutboxEventRecord event = event("SaleConfirmed");
        FakeRepository repository = new FakeRepository(List.of(event));
        List<UUID> delivered = new ArrayList<>();
        OutboxDispatcherService dispatcher = new OutboxDispatcherService(repository, deliveredEvent -> delivered.add(deliveredEvent.eventId()), CLOCK);

        OutboxDispatchReport report = dispatcher.dispatchPending(10, 5);

        assertThat(report).isEqualTo(new OutboxDispatchReport(1, 1, 0));
        assertThat(delivered).containsExactly(event.eventId());
        assertThat(repository.published).containsExactly(event.eventId());
        assertThat(repository.publishedAt).isEqualTo(Instant.parse("2026-07-21T12:00:00Z"));
        assertThat(repository.failed).isEmpty();
    }

    @Test
    void keepsDispatchingWhenOneEventFails() {
        OutboxEventRecord first = event("SaleConfirmed");
        OutboxEventRecord second = event("AccountingEntryPosted");
        FakeRepository repository = new FakeRepository(List.of(first, second));
        OutboxDispatcherService dispatcher = new OutboxDispatcherService(repository, event -> {
            if (event.eventId().equals(first.eventId())) {
                throw new IllegalStateException("temporary aws failure");
            }
        }, CLOCK);

        OutboxDispatchReport report = dispatcher.dispatchPending(10, 5);

        assertThat(report).isEqualTo(new OutboxDispatchReport(2, 1, 1));
        assertThat(repository.failed).containsExactly(first.eventId());
        assertThat(repository.lastError).isEqualTo("temporary aws failure");
        assertThat(repository.published).containsExactly(second.eventId());
    }

    @Test
    void rejectsInvalidDispatchParameters() {
        OutboxDispatcherService dispatcher = new OutboxDispatcherService(new FakeRepository(List.of()), event -> { }, CLOCK);

        assertThatThrownBy(() -> dispatcher.dispatchPending(0, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("batchSize must be greater than zero");
        assertThatThrownBy(() -> dispatcher.dispatchPending(1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxAttempts must be greater than zero");
    }

    private static OutboxEventRecord event(String type) {
        return new OutboxEventRecord(UUID.randomUUID(), type, 1, Instant.parse("2026-07-21T11:59:00Z"),
                UUID.randomUUID(), "SALE", UUID.randomUUID(), "billing-service", "corr", "idem", "{}");
    }

    private static final class FakeRepository implements OutboxEventRepositoryPort {
        private final List<OutboxEventRecord> events;
        private final List<UUID> published = new ArrayList<>();
        private final List<UUID> failed = new ArrayList<>();
        private Instant publishedAt;
        private String lastError;

        private FakeRepository(List<OutboxEventRecord> events) {
            this.events = events;
        }

        @Override
        public List<OutboxEventRecord> findDispatchable(int batchSize, int maxAttempts) {
            return events.stream().limit(batchSize).toList();
        }

        @Override
        public void markPublished(UUID eventId, Instant publishedAt) {
            published.add(eventId);
            this.publishedAt = publishedAt;
        }

        @Override
        public void markFailed(UUID eventId, String errorMessage) {
            failed.add(eventId);
            this.lastError = errorMessage;
        }
    }
}