package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AccountsPayablePaymentResponse(UUID id, UUID companyId, UUID accountsPayableId,
        LocalDate paymentDate, BigDecimal amount, String paymentMethod, String reference, UUID createdBy,
        Instant createdAt, AccountsPayableResponse payable) {
}
