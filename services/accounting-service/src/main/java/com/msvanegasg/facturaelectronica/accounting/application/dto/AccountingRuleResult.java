package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

public record AccountingRuleResult(
        UUID id,
        UUID companyId,
        AccountingEventType eventType,
        AccountingSourceType sourceType,
        String name,
        List<AccountingRuleLineResult> lines,
        boolean active,
        boolean used,
        long usageCount) {
}
