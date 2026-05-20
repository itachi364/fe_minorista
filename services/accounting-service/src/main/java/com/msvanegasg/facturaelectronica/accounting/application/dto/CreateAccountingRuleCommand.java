package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

public record CreateAccountingRuleCommand(
        UUID companyId,
        AccountingEventType eventType,
        AccountingSourceType sourceType,
        String name,
        List<CreateAccountingRuleLineCommand> lines) {
}
