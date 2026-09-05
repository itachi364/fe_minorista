package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;

public record AccountingReadinessResult(
        UUID companyId,
        AccountingEventType eventType,
        boolean ready,
        UUID accountingRuleId,
        List<String> checkedAccountCodes,
        List<AccountingReadinessMissingItemResult> missingItems) {
}
