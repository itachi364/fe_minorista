package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;

public record AssignFiscalNumberCommand(
        UUID companyId,
        ElectronicDocumentType documentType,
        LocalDate documentDate,
        FiscalEnvironment environment) {
}
