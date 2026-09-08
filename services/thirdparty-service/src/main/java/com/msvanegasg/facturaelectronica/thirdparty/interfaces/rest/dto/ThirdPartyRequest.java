package com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto;

import java.util.Set;

import com.msvanegasg.facturaelectronica.thirdparty.domain.model.PersonType;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.TaxRegime;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdPartyRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ThirdPartyRequest(
        @NotNull PersonType personType,
        @NotNull @Min(1) @Max(99) Integer identificationTypeCode,
        @NotBlank @Size(max = 30) String identificationNumber,
        Integer verificationDigit,
        @Size(max = 220) String fullName,
        @Size(max = 220) String businessName,
        @Size(max = 220) String tradeName,
        @Email @Size(max = 150) String email,
        @Size(max = 50) String phone,
        @Size(max = 250) String address,
        @Size(max = 20) String municipalityCode,
        @Size(max = 10) String ciiuCode,
        Set<@Size(max = 10) String> ciiuCodes,
        Set<@Size(max = 20) String> taxResponsibilities,
        TaxRegime taxRegime,
        @NotEmpty Set<ThirdPartyRole> roles) {

    public ThirdPartyRequest(PersonType personType, Integer identificationTypeCode, String identificationNumber,
            Integer verificationDigit, String fullName, String businessName, String tradeName, String email,
            String phone, String address, String municipalityCode, String ciiuCode,
            Set<String> taxResponsibilities, TaxRegime taxRegime, Set<ThirdPartyRole> roles) {
        this(personType, identificationTypeCode, identificationNumber, verificationDigit, fullName, businessName,
                tradeName, email, phone, address, municipalityCode, ciiuCode, null, taxResponsibilities, taxRegime,
                roles);
    }
}
