package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.time.LocalDate;

public record CompanyLicenseCommand(
        String planCode,
        LocalDate validFrom,
        LocalDate validTo,
        Integer maxUsers,
        Integer maxMonthlyDocuments) {
}
