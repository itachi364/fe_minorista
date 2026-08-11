package com.msvanegasg.facturaelectronica.payroll.application.dto;

public record PayrollSettingsCommand(boolean electronicPayrollEnabled, String providerMode) {
}
