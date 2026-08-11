package com.msvanegasg.facturaelectronica.payroll.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DailyLaborPaymentCommand(UUID workerId, LocalDate workDate, String activityDescription,
        BigDecimal agreedAmount, BigDecimal paidAmount, String paymentMethodCode, boolean legalNoticeAccepted,
        String notes) {
}
