package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AccountsPayablePaymentResult(UUID id, UUID companyId, UUID accountsPayableId, LocalDate paymentDate,
        BigDecimal amount, String paymentMethod, String reference, UUID createdBy, Instant createdAt,
        AccountsPayableResult payable) {
}
