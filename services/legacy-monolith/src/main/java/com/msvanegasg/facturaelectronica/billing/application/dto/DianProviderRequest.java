package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

public record DianProviderRequest(
        UUID companyId,
        UUID documentId,
        ElectronicDocumentType documentType,
        String prefix,
        long number,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal total,
        String payloadXml,
        String idempotencyKey) {
}
