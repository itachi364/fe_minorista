package com.msvanegasg.facturaelectronica.inventorylambda;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;

public class ProcessInventorySaleEffectService {

    private final SaleConfirmedInventoryEffectMapper mapper;
    private final InventorySaleEffectRepositoryPort repository;

    public ProcessInventorySaleEffectService(SaleConfirmedInventoryEffectMapper mapper,
            InventorySaleEffectRepositoryPort repository) {
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
        this.repository = Objects.requireNonNull(repository, "repository is required");
    }

    public InventorySaleEffectResult process(DomainEventEnvelope envelope) {
        if (!mapper.supports(envelope)) {
            return InventorySaleEffectResult.asIgnored();
        }
        InventorySaleEffectRequest request = mapper.toRequest(envelope);
        boolean saved = repository.applyIfNew(envelope, request);
        return saved ? InventorySaleEffectResult.asProcessed(request.stockTrackedLines().size())
                : InventorySaleEffectResult.asDuplicate();
    }
}
