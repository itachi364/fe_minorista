package com.msvanegasg.facturaelectronica.eventing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepositoryPort {

    List<OutboxEventRecord> findDispatchable(int batchSize, int maxAttempts);

    void markPublished(UUID eventId, Instant publishedAt);

    void markFailed(UUID eventId, String errorMessage);
}