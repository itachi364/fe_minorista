package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.util.List;

public record FiscalCsvValidationResult(boolean valid, int rowCount, int packageCount, List<String> errors) {
    public FiscalCsvValidationResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
}
