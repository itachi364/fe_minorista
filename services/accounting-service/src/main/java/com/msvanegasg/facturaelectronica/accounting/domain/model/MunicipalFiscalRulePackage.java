package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MunicipalFiscalRulePackage(
        UUID id,
        String code,
        String version,
        String municipalityCode,
        String municipalityName,
        UUID legalSourceId,
        FiscalRuleSetStatus status,
        LocalDate validFrom,
        LocalDate validTo,
        Instant reviewedAt,
        Instant publishedAt,
        UUID publishedBy,
        UUID importId,
        Instant createdAt,
        UUID createdBy,
        int ruleCount) {
}
