package com.msvanegasg.facturaelectronica.payroll.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DailyLaborPaymentRequest(
        @NotNull UUID workerId,
        @NotNull LocalDate workDate,
        @NotBlank @Size(max = 300) String activityDescription,
        @NotNull @Positive BigDecimal agreedAmount,
        @NotNull @Positive BigDecimal paidAmount,
        @NotBlank @Size(max = 40) String paymentMethodCode,
        boolean legalNoticeAccepted,
        @Size(max = 500) String notes) {
}
