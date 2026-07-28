package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AccountsReceivablePaymentResponse(UUID id, UUID companyId, UUID accountsReceivableId,
        LocalDate paymentDate, BigDecimal amount, String paymentMethod, String reference, UUID createdBy,
        Instant createdAt, AccountsReceivableResponse receivable) {
}