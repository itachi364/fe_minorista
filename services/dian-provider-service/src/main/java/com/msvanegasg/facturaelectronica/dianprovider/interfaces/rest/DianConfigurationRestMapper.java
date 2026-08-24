package com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianConfigurationCommand;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianConfigurationResult;
import com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest.dto.DianConfigurationRequest;
import com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest.dto.DianConfigurationResponse;

final class DianConfigurationRestMapper {

    private DianConfigurationRestMapper() {
    }

    static DianConfigurationCommand toCommand(UUID companyId, UUID userId, DianConfigurationRequest request) {
        return new DianConfigurationCommand(companyId, request.mode(), request.environment(), request.softwareId(),
                request.softwarePin(), request.technicalKey(), request.certificatePayload(),
                request.certificatePassword(), request.certificateAlias(), request.certificateFingerprint(),
                request.certificateExpiresAt(), request.serviceBaseUrl(), request.testSetId(),
                request.acceptedResponsibility(), userId);
    }

    static DianConfigurationResponse toResponse(DianConfigurationResult result) {
        return new DianConfigurationResponse(result.id(), result.companyId(), result.mode(), result.environment(),
                result.softwareId(), result.softwarePinConfigured(), result.technicalKeyConfigured(),
                result.certificateConfigured(), result.certificateAlias(), result.certificateFingerprint(),
                result.certificateExpiresAt(), result.serviceBaseUrl(), result.testSetId(),
                result.acceptedResponsibility(), result.status(), result.lastTestStatus(), result.lastTestAt(),
                result.lastTestMessage(), result.createdAt(), result.updatedAt());
    }
}
