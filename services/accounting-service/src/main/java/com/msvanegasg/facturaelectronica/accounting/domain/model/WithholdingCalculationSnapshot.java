package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record WithholdingCalculationSnapshot(
        UUID id,
        UUID companyId,
        AccountingSourceType sourceType,
        UUID sourceId,
        UUID thirdPartyId,
        LocalDate operationDate,
        WithholdingType withholdingType,
        BigDecimal baseAmount,
        BigDecimal rate,
        BigDecimal amount,
        String ruleVersion,
        WithholdingDecision decision,
        String reason,
        Instant createdAt,
        UUID ruleId,
        String parameterVersion,
        String legalReference,
        String sourceUrl) {

    public WithholdingCalculationSnapshot(UUID id, UUID companyId, AccountingSourceType sourceType, UUID sourceId,
            UUID thirdPartyId, LocalDate operationDate, WithholdingType withholdingType, BigDecimal baseAmount,
            BigDecimal rate, BigDecimal amount, String ruleVersion, WithholdingDecision decision, String reason,
            Instant createdAt) {
        this(id, companyId, sourceType, sourceId, thirdPartyId, operationDate, withholdingType, baseAmount, rate,
                amount, ruleVersion, decision, reason, createdAt, null, null, null, null);
    }
}
