package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseStatus;

public record PurchaseResponse(UUID id, UUID companyId, UUID supplierId, PurchaseStatus status, BigDecimal subtotal,
        BigDecimal taxTotal, BigDecimal total, String evidenceUrl, String idempotencyKey, Instant createdAt,
        Instant confirmedAt, List<PurchaseLineResponse> lines) {
}
