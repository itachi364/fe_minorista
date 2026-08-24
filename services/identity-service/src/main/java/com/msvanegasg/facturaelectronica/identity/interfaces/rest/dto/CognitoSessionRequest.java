package com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CognitoSessionRequest(
        @NotBlank String subject,
        @NotBlank @Email String email,
        String fullName,
        Set<String> groups) {
}
