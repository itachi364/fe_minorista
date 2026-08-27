package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

public record AccountingRuleResponse(
        UUID id,
        UUID companyId,
        AccountingEventType eventType,
        AccountingSourceType sourceType,
        String name,
        List<AccountingRuleLineResponse> lines,
        boolean active,
        boolean used,
        long usageCount) {
}
