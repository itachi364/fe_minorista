package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;

public record NumberingResolutionResult(
        UUID id,
        UUID companyId,
        ElectronicDocumentType documentType,
        String resolutionNumber,
        String prefix,
        long fromNumber,
        long toNumber,
        long currentNumber,
        LocalDate validFrom,
        LocalDate validTo,
        FiscalEnvironment environment,
        boolean active,
        boolean used,
        long usageCount) {
}
