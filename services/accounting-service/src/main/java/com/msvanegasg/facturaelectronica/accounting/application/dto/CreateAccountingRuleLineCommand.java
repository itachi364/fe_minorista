package com.msvanegasg.facturaelectronica.accounting.application.dto;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingAmountType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntrySide;

public record CreateAccountingRuleLineCommand(
        String accountCode,
        AccountingEntrySide side,
        AccountingAmountType amountType,
        String description) {
}
