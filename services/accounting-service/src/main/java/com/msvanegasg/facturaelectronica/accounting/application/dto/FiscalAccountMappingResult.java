package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

public record FiscalAccountMappingResult(UUID id, UUID companyId, WithholdingType withholdingType,
        String payableAccountCode, String receivableAccountCode, LocalDate validFrom, LocalDate validTo,
        boolean active) {
}
