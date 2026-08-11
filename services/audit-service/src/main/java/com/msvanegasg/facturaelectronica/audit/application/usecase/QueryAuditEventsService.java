package com.msvanegasg.facturaelectronica.audit.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.audit.application.dto.AuditEventQuery;
import com.msvanegasg.facturaelectronica.audit.application.dto.AuditEventResult;
import com.msvanegasg.facturaelectronica.audit.application.port.in.QueryAuditEventsUseCase;
import com.msvanegasg.facturaelectronica.audit.application.port.out.AuditEventRepositoryPort;

public class QueryAuditEventsService implements QueryAuditEventsUseCase {

    private final AuditEventRepositoryPort repository;

    public QueryAuditEventsService(AuditEventRepositoryPort repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public List<AuditEventResult> find(AuditEventQuery query) {
        Objects.requireNonNull(query, "query is required");
        Objects.requireNonNull(query.companyId(), "companyId is required");
        if (query.from() != null && query.to() != null && query.from().isAfter(query.to())) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }
        return repository.find(query).stream().map(AuditEventResultMapper::toResult).toList();
    }

    @Override
    public List<String> resourceTypes(UUID companyId) {
        Objects.requireNonNull(companyId, "companyId is required");
        return repository.resourceTypes(companyId);
    }
}
