package com.msvanegasg.facturaelectronica.dianprovider.application.dto;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianConfigurationStatus;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianConnectionMode;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianEnvironment;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianTestStatus;

public record DianConfigurationResult(
        UUID id,
        UUID companyId,
        DianConnectionMode mode,
        DianEnvironment environment,
        String softwareId,
        boolean softwarePinConfigured,
        boolean technicalKeyConfigured,
        boolean certificateConfigured,
        String certificateAlias,
        String certificateFingerprint,
        Instant certificateExpiresAt,
        String serviceBaseUrl,
        String testSetId,
        boolean acceptedResponsibility,
        DianConfigurationStatus status,
        DianTestStatus lastTestStatus,
        Instant lastTestAt,
        String lastTestMessage,
        Instant createdAt,
        Instant updatedAt) {
}
