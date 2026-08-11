package com.msvanegasg.facturaelectronica.payroll.interfaces.rest.dto;

public record PayrollSettingsRequest(boolean electronicPayrollEnabled, String providerMode) {
}
