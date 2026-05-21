package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterPayablePaymentCommand(UUID companyId, UUID payableId, LocalDate paymentDate, BigDecimal amount,
        String paymentMethod, String reference, UUID createdBy) {
}
