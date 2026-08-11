package com.msvanegasg.facturaelectronica.identity.application.dto;

import java.util.UUID;

public record UpdateUserCommand(UUID companyId, UUID userId, String email, String fullName,
        String authorizationHeader) {
}
