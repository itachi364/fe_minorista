package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;

public record ElectronicPosDocumentResult(
        UUID id,
        UUID companyId,
        UUID saleId,
        String buyerName,
        String buyerDocumentType,
        String buyerDocumentNumber,
        String prefix,
        long number,
        String cude,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal total,
        ElectronicDocumentStatus status,
        Instant issueAt) {
}
