package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

public record CalculateElectronicDocumentCommand(
        UUID companyId,
        ElectronicDocumentType documentType,
        List<ElectronicDocumentLineCalculationCommand> lines) {

    public CalculateElectronicDocumentCommand {
        lines = List.copyOf(lines);
    }
}
