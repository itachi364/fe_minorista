package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AccountsReceivablePaymentResult(UUID id, UUID companyId, UUID accountsReceivableId,
        LocalDate paymentDate, BigDecimal amount, String paymentMethod, String reference, UUID createdBy,
        Instant createdAt, AccountsReceivableResult receivable) {
}