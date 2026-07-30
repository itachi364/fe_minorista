package com.msvanegasg.facturaelectronica.thirdparty.application.dto;

import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.thirdparty.domain.model.PersonType;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdPartyRole;

public record ThirdPartyResult(
        UUID id,
        UUID companyId,
        PersonType personType,
        String identificationTypeCode,
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
