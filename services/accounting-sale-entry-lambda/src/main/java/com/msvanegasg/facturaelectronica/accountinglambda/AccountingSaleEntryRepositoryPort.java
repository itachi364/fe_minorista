package com.msvanegasg.facturaelectronica.accountinglambda;

import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;

public interface AccountingSaleEntryRepositoryPort {

    boolean applyIfNew(DomainEventEnvelope envelope, AccountingSaleEntryRequest request);
}