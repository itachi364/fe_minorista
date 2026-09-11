package com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto;

import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.thirdparty.domain.model.PersonType;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.IncomeTaxStatus;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.TaxResidency;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.TaxRegime;
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
        String ciiuCode,
        Set<String> ciiuCodes,
        Set<String> taxResponsibilities,
        TaxRegime taxRegime,
        TaxResidency taxResidency,
        IncomeTaxStatus incomeTaxStatus,
        Set<String> selfWithholdingScopes,
        String fiscalEvidenceReference,
        Set<ThirdPartyRole> roles,
        boolean active) {

    public ThirdPartyResponse(UUID id, UUID companyId, PersonType personType, Integer identificationTypeCode,
            String identificationNumber, Integer verificationDigit, String fullName, String businessName,
            String tradeName, String email, String phone, String address, String municipalityCode, String ciiuCode,
            Set<String> taxResponsibilities, TaxRegime taxRegime, Set<ThirdPartyRole> roles, boolean active) {
        this(id, companyId, personType, identificationTypeCode, identificationNumber, verificationDigit, fullName,
                businessName, tradeName, email, phone, address, municipalityCode, ciiuCode,
                ciiuCode == null ? Set.of() : Set.of(ciiuCode), taxResponsibilities, taxRegime,
                TaxResidency.UNKNOWN, IncomeTaxStatus.UNKNOWN, Set.of(), null, roles, active);
    }
}
