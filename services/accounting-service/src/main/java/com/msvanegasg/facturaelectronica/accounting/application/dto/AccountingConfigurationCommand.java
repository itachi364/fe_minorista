package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.util.List;
import java.util.UUID;

public record AccountingConfigurationCommand(
        UUID companyId,
        List<CreateAccountCommand> accounts,
        List<CreateAccountingRuleCommand> rules) {
}
