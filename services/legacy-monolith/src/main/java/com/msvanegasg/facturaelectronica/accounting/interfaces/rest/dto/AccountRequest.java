package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record AccountRequest(
        @NotBlank String code,
        @NotBlank String name,
        UUID parentAccountId) {
}
