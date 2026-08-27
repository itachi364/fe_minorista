package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.util.List;

import jakarta.validation.Valid;

public record AccountingConfigurationRequest(
        @Valid List<AccountRequest> accounts,
        @Valid List<AccountingRuleRequest> rules) {
}
