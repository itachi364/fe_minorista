package com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @Email @NotBlank String email,
        @NotBlank String fullName) {
}
