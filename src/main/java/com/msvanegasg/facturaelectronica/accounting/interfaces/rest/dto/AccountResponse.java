package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountCategory;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountLevel;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountNature;

public record AccountResponse(
        UUID id,
        UUID companyId,
        String code,
        String name,
        AccountCategory category,
        AccountLevel level,
        AccountNature nature,
        UUID parentAccountId,
        boolean active) {
}
