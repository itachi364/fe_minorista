package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseStatus;

public record PurchaseResult(UUID id, UUID companyId, UUID supplierId, PurchaseStatus status, BigDecimal subtotal,
        BigDecimal taxTotal, BigDecimal total, String evidenceUrl, String idempotencyKey, Instant createdAt,
        Instant confirmedAt, List<PurchaseLineResult> lines) {
}
