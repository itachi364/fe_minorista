package com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto;

import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.thirdparty.domain.model.PersonType;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdPartyRole;

public record ThirdPartyResponse(
        UUID id,
        UUID companyId,
        PersonType personType,
        Integer identificationTypeCode,
        String identificationNumber,
        Integer verificationDigit,
        String fullName,
        String businessName,
        String tradeName,
        String email,
        String phone,
        String address,
        String municipalityCode,
        Set<ThirdPartyRole> roles,
        boolean active) {
}
