package com.msvanegasg.facturaelectronica.thirdparty.application.dto;

import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.thirdparty.domain.model.PersonType;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.IncomeTaxStatus;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.TaxResidency;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.TaxRegime;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdPartyRole;

public record ThirdPartyCommand(
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
        Set<ThirdPartyRole> roles) {

    public ThirdPartyCommand(UUID companyId, PersonType personType, Integer identificationTypeCode,
            String identificationNumber, Integer verificationDigit, String fullName, String businessName,
            String tradeName, String email, String phone, String address, String municipalityCode, String ciiuCode,
            Set<String> taxResponsibilities, TaxRegime taxRegime, Set<ThirdPartyRole> roles) {
        this(companyId, personType, identificationTypeCode, identificationNumber, verificationDigit, fullName,
                businessName, tradeName, email, phone, address, municipalityCode, ciiuCode, null,
                taxResponsibilities, taxRegime, TaxResidency.UNKNOWN, IncomeTaxStatus.UNKNOWN, Set.of(), null, roles);
    }
}
