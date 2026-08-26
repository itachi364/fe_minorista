package com.msvanegasg.facturaelectronica.identity.application.dto;

import java.util.UUID;

public record OperationalPinCommand(UUID companyId, String pin, String authorizationHeader) {
}
