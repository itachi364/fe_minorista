package com.msvanegasg.facturaelectronica.inventory.infrastructure.messaging;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.eventing.OutboxEventRecord;
import com.msvanegasg.facturaelectronica.eventing.OutboxEventRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.messaging.entity.OutboxEventJpaEntity;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.messaging.repository.OutboxEventJpaRepository;

@Component
public class OutboxDispatchRepositoryAdapter implements OutboxEventRepositoryPort {

    private static final List<String> DISPATCHABLE_STATUSES = List.of("PENDING", "FAILED");

    private final OutboxEventJpaRepository repository;

    public OutboxDispatchRepositoryAdapter(OutboxEventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEventRecord> findDispatchable(int batchSize, int maxAttempts) {
        return repository.findDispatchable(DISPATCHABLE_STATUSES, maxAttempts, PageRequest.of(0, batchSize)).stream()
                .map(OutboxEventJpaEntity::toRecord)
                .toList();
    }

    @Override
    @Transactional
    public void markPublished(UUID eventId, Instant publishedAt) {
        OutboxEventJpaEntity event = repository.findById(eventId).orElseThrow();
        event.markPublished(publishedAt);
        repository.save(event);
    }

    @Override
    @Transactional
    public void markFailed(UUID eventId, String errorMessage) {
        OutboxEventJpaEntity event = repository.findById(eventId).orElseThrow();
        event.markFailed(errorMessage);
        repository.save(event);
    }
}
