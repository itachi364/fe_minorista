package com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest.dto;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ProviderSubmissionRequest(
        @NotNull UUID companyId,
        @NotNull UUID documentId,
        Map<String, Object> payload) {
}
