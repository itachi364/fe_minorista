package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;

public record IssueElectronicPosCommand(
        UUID companyId,
        UUID saleId,
        String buyerName,
        String buyerDocumentType,
        String buyerDocumentNumber,
        LocalDate documentDate,
        FiscalEnvironment environment,
        List<ElectronicDocumentLineCalculationCommand> lines) {

    public IssueElectronicPosCommand {
        lines = List.copyOf(lines);
    }
}
