package com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest.dto;

import java.time.Instant;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianConnectionMode;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianEnvironment;

import jakarta.validation.constraints.NotNull;

public record DianConfigurationRequest(
        @NotNull DianConnectionMode mode,
        @NotNull DianEnvironment environment,
        String softwareId,
        String softwarePin,
        String technicalKey,
        String certificatePayload,
        String certificatePassword,
        String certificateAlias,
        String certificateFingerprint,
        Instant certificateExpiresAt,
        String serviceBaseUrl,
        String testSetId,
        boolean acceptedResponsibility) {
}
