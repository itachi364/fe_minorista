package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.util.List;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AccountingRuleRequest(
        @NotNull AccountingEventType eventType,
        @NotNull AccountingSourceType sourceType,
        @NotBlank String name,
        @Valid @NotEmpty List<AccountingRuleLineRequest> lines) {
}
