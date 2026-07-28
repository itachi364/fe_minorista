package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.util.List;
import java.util.UUID;

public record AccountingSetupResult(
        UUID companyId,
        String templateName,
        List<AccountResult> accounts,
        List<AccountingRuleResult> rules) {
}