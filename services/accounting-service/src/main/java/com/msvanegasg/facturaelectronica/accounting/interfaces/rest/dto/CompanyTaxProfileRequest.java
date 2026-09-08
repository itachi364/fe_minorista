package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.util.Set;

public record CompanyTaxProfileRequest(
        String taxRegime,
        Set<String> rutResponsibilities,
        boolean vatResponsible,
        boolean withholdingAgent,
        boolean largeTaxpayer,
        boolean selfWithholding,
        boolean simpleRegime,
        String icaMunicipalityCode,
        Set<String> ciiuCodes,
        Boolean vatWithholdingAgent,
        Boolean icaWithholdingAgent) {
}
