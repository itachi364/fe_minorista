package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateMunicipalFiscalPackageCommand(
        String municipalityCode,
        String packageCode,
        String version,
        LocalDate validFrom,
        LocalDate validTo,
        String legalReference,
        String officialSourceUrl,
        List<MunicipalReteicaRuleCommand> rules,
        UUID userId) {
}
