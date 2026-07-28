package com.msvanegasg.facturaelectronica.eventing;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class OutboxDispatcherService {

    private final OutboxEventRepositoryPort repository;
    private final OutboxEventDeliveryPort delivery;
    private final Clock clock;

    public OutboxDispatcherService(OutboxEventRepositoryPort repository, OutboxEventDeliveryPort delivery, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository is required");
        this.delivery = Objects.requireNonNull(delivery, "delivery is required");
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    public OutboxDispatchReport dispatchPending(int batchSize, int maxAttempts) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be greater than zero");
        }
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be greater than zero");
        }
        List<OutboxEventRecord> events = repository.findDispatchable(batchSize, maxAttempts);
        int published = 0;
        int failed = 0;
        for (OutboxEventRecord event : events) {
            try {
                delivery.deliver(event);
                repository.markPublished(event.eventId(), Instant.now(clock));
                published++;
            } catch (RuntimeException exception) {
                repository.markFailed(event.eventId(), safeMessage(exception));
                failed++;
            }
        }
        return new OutboxDispatchReport(events.size(), published, failed);
    }

    private static String safeMessage(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}