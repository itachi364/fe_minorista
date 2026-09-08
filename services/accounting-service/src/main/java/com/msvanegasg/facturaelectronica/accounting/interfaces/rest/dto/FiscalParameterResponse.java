package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FiscalParameterResponse(UUID id, String code, String version, BigDecimal value, LocalDate validFrom,
        LocalDate validTo, String legalReference, String sourceUrl, boolean published) {
}
