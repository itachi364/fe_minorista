package com.msvanegasg.facturaelectronica.payroll.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record DailyLaborPayment(UUID id, UUID companyId, UUID workerId, LocalDate workDate, String activityDescription,
        BigDecimal agreedAmount, BigDecimal paidAmount, String paymentMethodCode, boolean legalNoticeAccepted,
        String notes, Instant createdAt) {

    public DailyLaborPayment {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(workerId, "workerId is required");
        Objects.requireNonNull(workDate, "workDate is required");
        activityDescription = required(activityDescription, "activityDescription");
        agreedAmount = positive(agreedAmount, "agreedAmount");
        paidAmount = positive(paidAmount, "paidAmount");
        paymentMethodCode = required(paymentMethodCode, "paymentMethodCode").toUpperCase();
        if (!legalNoticeAccepted) {
            throw new IllegalArgumentException("legalNoticeAccepted is required for verbal daily payments");
        }
        notes = notes == null || notes.isBlank() ? null : notes.trim();
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static BigDecimal positive(BigDecimal value, String field) {
        Objects.requireNonNull(value, field + " is required");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }
}
