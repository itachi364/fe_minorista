package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.util.List;
import java.util.UUID;

public record AccountingSetupResponse(
        UUID companyId,
        String templateName,
        List<AccountResponse> accounts,
        List<AccountingRuleResponse> rules) {
}