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
        Instant createdAt) {
}
