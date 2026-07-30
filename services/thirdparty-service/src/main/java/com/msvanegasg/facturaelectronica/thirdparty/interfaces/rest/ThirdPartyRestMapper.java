package com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.thirdparty.application.dto.ThirdPartyCommand;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.ThirdPartyResult;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto.ThirdPartyRequest;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto.ThirdPartyResponse;

public final class ThirdPartyRestMapper {

    private ThirdPartyRestMapper() {
    }

    public static ThirdPartyCommand toCommand(UUID companyId, ThirdPartyRequest request) {
        return new ThirdPartyCommand(companyId, request.personType(), request.identificationTypeCode(),
                request.identificationNumber(), request.verificationDigit(), request.fullName(),
                request.businessName(), request.tradeName(), request.email(), request.phone(), request.address(),
                request.municipalityCode(), request.roles());
    }

    public static ThirdPartyResponse toResponse(ThirdPartyResult result) {
        return new ThirdPartyResponse(result.id(), result.companyId(), result.personType(),
                result.identificationTypeCode(), result.identificationNumber(), result.verificationDigit(),
                result.fullName(), result.businessName(), result.tradeName(), result.email(), result.phone(),
                result.address(), result.municipalityCode(), result.roles(), result.active());
    }
}
