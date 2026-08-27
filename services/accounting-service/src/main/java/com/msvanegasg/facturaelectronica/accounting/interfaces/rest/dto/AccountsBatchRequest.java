package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record AccountsBatchRequest(
        @Valid @NotEmpty List<AccountRequest> accounts) {
}
