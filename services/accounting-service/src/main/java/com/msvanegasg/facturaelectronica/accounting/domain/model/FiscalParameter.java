package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record FiscalParameter(UUID id, String code, String version, BigDecimal value, LocalDate validFrom,
        LocalDate validTo, String legalReference, String sourceUrl, boolean published) {

    public FiscalParameter {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(code, "code is required");
        Objects.requireNonNull(version, "version is required");
        Objects.requireNonNull(value, "value is required");
        Objects.requireNonNull(validFrom, "validFrom is required");
        if (value.signum() < 0 || validTo != null && validTo.isBefore(validFrom)) {
            throw new IllegalArgumentException("invalid fiscal parameter");
        }
    }
}
