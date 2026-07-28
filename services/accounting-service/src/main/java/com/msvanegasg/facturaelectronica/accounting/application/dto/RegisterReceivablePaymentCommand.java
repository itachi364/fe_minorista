package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterReceivablePaymentCommand(UUID companyId, UUID receivableId, LocalDate paymentDate,
        BigDecimal amount, String paymentMethod, String reference, UUID createdBy) {
}