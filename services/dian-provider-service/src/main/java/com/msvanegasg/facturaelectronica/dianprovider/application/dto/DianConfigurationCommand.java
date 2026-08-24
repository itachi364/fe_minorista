package com.msvanegasg.facturaelectronica.dianprovider.application.dto;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianConnectionMode;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianEnvironment;

public record DianConfigurationCommand(
        UUID companyId,
        DianConnectionMode mode,
        DianEnvironment environment,
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
        boolean acceptedResponsibility,
        UUID updatedBy) {
}
