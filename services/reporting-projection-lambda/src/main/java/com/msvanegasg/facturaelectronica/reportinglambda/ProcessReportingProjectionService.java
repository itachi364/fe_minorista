package com.msvanegasg.facturaelectronica.reportinglambda;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;

public class ProcessReportingProjectionService {

    private final ReportingProjectionMapper mapper;
    private final ReportingProjectionRepositoryPort repository;

    public ProcessReportingProjectionService(ReportingProjectionMapper mapper,
            ReportingProjectionRepositoryPort repository) {
        this.mapper = Objects.requireNonNull(mapper, "mapper is required");
        this.repository = Objects.requireNonNull(repository, "repository is required");
    }

    public ReportingProjectionResult process(DomainEventEnvelope envelope) {
        if (!mapper.supports(envelope)) {
            return ReportingProjectionResult.ignoredResult();
        }
        boolean inserted = repository.projectIfNew(mapper.toRequest(envelope));
        return inserted ? ReportingProjectionResult.processedResult() : ReportingProjectionResult.duplicateResult();
    }
}