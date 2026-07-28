package com.msvanegasg.facturaelectronica.eventing;

@FunctionalInterface
public interface OutboxEventDeliveryPort {

    void deliver(OutboxEventRecord event);
}