package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FiscalAuxiliaryResult(UUID companyId, int year, int month, String conceptCode,
        String form350Section, String withholdingType, BigDecimal baseAmount,
        BigDecimal withheldAmount, long documentCount) {
}
