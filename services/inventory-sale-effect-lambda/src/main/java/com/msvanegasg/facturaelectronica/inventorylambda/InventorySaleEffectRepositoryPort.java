package com.msvanegasg.facturaelectronica.inventorylambda;

import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;

public interface InventorySaleEffectRepositoryPort {

    boolean applyIfNew(DomainEventEnvelope envelope, InventorySaleEffectRequest request);
}
