package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AccountPresentationMappingResult(UUID id, UUID companyId, UUID accountId,
        String accountCode, String financialReportingGroup, String statementSection,
        String presentationConcept, LocalDate validFrom, LocalDate validTo,
        String evidenceReference, boolean active) {
}
