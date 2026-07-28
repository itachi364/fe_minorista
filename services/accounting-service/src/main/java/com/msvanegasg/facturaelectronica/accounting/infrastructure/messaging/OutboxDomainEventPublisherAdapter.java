package com.msvanegasg.facturaelectronica.accounting.infrastructure.messaging;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.DomainEventPublisherPort;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.messaging.entity.OutboxEventJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.messaging.repository.OutboxEventJpaRepository;

@Component
public class OutboxDomainEventPublisherAdapter implements DomainEventPublisherPort {

    private final OutboxEventJpaRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxDomainEventPublisherAdapter(OutboxEventJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void publish(DomainEventEnvelope event) {
        try {
            String payloadJson = objectMapper.writeValueAsString(event.payload());
            repository.save(OutboxEventJpaEntity.from(event, payloadJson));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("event payload could not be serialized", exception);
        }
    }
}
