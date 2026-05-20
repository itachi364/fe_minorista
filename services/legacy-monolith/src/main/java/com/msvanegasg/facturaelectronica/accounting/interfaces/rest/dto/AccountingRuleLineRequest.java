package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingAmountType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntrySide;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountingRuleLineRequest(
        @NotBlank String accountCode,
        @NotNull AccountingEntrySide side,
        @NotNull AccountingAmountType amountType,
        String description) {
}
