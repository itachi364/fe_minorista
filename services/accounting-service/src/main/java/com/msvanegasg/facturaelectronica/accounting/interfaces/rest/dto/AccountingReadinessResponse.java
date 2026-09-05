package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;

public record AccountingReadinessResponse(
        UUID companyId,
        AccountingEventType eventType,
        boolean ready,
        UUID accountingRuleId,
        List<String> checkedAccountCodes,
        List<AccountingReadinessMissingItemResponse> missingItems) {
}
