package com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record OperationalPinRequest(@NotBlank String pin) {
}
