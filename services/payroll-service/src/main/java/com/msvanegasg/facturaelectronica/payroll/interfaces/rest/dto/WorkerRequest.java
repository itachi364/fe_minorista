package com.msvanegasg.facturaelectronica.payroll.interfaces.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkerRequest(
        @Min(1) @Max(99) int identificationTypeCode,
        @NotBlank @Size(max = 40) String identificationNumber,
        Integer verificationDigit,
        @NotBlank @Size(max = 180) String fullName,
        @NotBlank @Size(max = 40) String workerClassification,
        Boolean active) {
}
