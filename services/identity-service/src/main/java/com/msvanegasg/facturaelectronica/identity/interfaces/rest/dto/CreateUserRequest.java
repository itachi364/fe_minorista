package com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(max = 180) String fullName,
        @NotBlank @Size(min = 8, max = 120) String password) {
}
