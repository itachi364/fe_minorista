package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.util.Set;

public record CompanyTaxProfileCommand(
        String taxRegime,
        Set<String> rutResponsibilities,
        boolean vatResponsible,
        boolean withholdingAgent,
        boolean largeTaxpayer,
        boolean selfWithholding,
        boolean simpleRegime,
        String icaMunicipalityCode,
        Set<String> ciiuCodes) {
}
