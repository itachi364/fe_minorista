package com.msvanegasg.facturaelectronica.payroll.interfaces.rest.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ElectronicPayrollDocumentRequest(@NotNull UUID dailyLaborPaymentId) {
}
