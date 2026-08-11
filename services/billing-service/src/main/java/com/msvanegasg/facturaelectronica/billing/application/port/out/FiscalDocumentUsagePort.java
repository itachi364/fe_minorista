package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.time.Instant;
import java.util.UUID;

@FunctionalInterface
public interface FiscalDocumentUsagePort {

    long countIssuedDocuments(UUID companyId, Instant fromInclusive, Instant toExclusive);

    static FiscalDocumentUsagePort noop() {
        return (companyId, fromInclusive, toExclusive) -> 0L;
    }
}
