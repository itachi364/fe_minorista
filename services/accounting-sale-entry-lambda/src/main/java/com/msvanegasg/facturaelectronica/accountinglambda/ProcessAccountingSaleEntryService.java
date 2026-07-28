package com.msvanegasg.facturaelectronica.accountinglambda;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;

public class ProcessAccountingSaleEntryService {

    private final SaleConfirmedAccountingEntryMapper mapper;
    private final AccountingSaleEntryRepositoryPort repository;

    public ProcessAccountingSaleEntryService(SaleConfirmedAccountingEntryMapper mapper,
            AccountingSaleEntryRepositoryPort repository) {
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
        this.repository = Objects.requireNonNull(repository, "repository is required");
    }

    public AccountingSaleEntryResult process(DomainEventEnvelope envelope) {
        if (!mapper.supports(envelope)) {
            return AccountingSaleEntryResult.ignoredResult();
        }
        AccountingSaleEntryRequest request = mapper.toRequest(envelope);
        boolean applied = repository.applyIfNew(envelope, request);
        if (!applied) {
            return AccountingSaleEntryResult.duplicateResult();
        }
        return AccountingSaleEntryResult.processed(true);
    }
}