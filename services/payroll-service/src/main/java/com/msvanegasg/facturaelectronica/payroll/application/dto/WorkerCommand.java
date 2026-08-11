package com.msvanegasg.facturaelectronica.payroll.application.dto;

public record WorkerCommand(int identificationTypeCode, String identificationNumber, Integer verificationDigit,
        String fullName, String workerClassification, Boolean active) {
}
