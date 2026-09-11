package com.msvanegasg.facturaelectronica.accounting.application.dto;

public record NationalFiscalConceptResult(String code, String description, String withholdingType,
        String form350Section, String operationalStatus, String legalReference, String sourceUrl) {
}
