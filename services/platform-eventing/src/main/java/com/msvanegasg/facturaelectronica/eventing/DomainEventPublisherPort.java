package com.msvanegasg.facturaelectronica.eventing;

@FunctionalInterface
public interface DomainEventPublisherPort {

    void publish(DomainEventEnvelope event);

    static DomainEventPublisherPort noop() {
        return event -> { };
    }
}
