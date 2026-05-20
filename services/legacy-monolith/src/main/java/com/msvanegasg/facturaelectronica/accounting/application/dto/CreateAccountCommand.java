package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.util.UUID;

public record CreateAccountCommand(
        UUID companyId,
        String code,
        String name,
        UUID parentAccountId) {
}
