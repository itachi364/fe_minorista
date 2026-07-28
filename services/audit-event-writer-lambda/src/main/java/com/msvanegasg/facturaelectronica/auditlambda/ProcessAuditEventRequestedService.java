package com.msvanegasg.facturaelectronica.auditlambda;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;

public class ProcessAuditEventRequestedService {

    private final AuditEventRequestedMapper mapper;
    private final AuditEventInboxRepositoryPort repository;

    public ProcessAuditEventRequestedService(AuditEventRequestedMapper mapper, AuditEventInboxRepositoryPort repository) {
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
        this.repository = Objects.requireNonNull(repository, "repository is required");
    }

    public AuditEventWriterResult process(DomainEventEnvelope envelope) {
        if (!mapper.supports(envelope)) {
            return AuditEventWriterResult.asIgnored();
        }
        AuditEventWriteRequest request = mapper.toRequest(envelope);
        boolean saved = repository.saveIfNew(envelope, request);
        return saved ? AuditEventWriterResult.asProcessed() : AuditEventWriterResult.asDuplicate();
    }
}
