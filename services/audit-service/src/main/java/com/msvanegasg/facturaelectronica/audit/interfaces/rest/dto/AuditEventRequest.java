package com.msvanegasg.facturaelectronica.audit.interfaces.rest.dto;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.audit.domain.model.AuditResult;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AuditEventRequest(
        UUID userId,
        @NotBlank @Size(max = 80) String eventType,
        @NotBlank @Size(max = 80) String resourceType,
        @Size(max = 120) String resourceId,
        @NotBlank @Size(max = 80) String action,
        @NotNull AuditResult result,
        String detail) {
}
