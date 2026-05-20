package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingAmountType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntrySide;

public record AccountingRuleLineResponse(
        String accountCode,
        AccountingEntrySide side,
        AccountingAmountType amountType,
        String description) {
}
