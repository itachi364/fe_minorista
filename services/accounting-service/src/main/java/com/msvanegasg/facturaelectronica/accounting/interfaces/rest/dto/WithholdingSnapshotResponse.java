package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingDecision;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

public record WithholdingSnapshotResponse(
        UUID id,
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
        UUID ruleId,
        String parameterVersion,
        String legalReference,
        String sourceUrl,
        Instant createdAt) {
}
