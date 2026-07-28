package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseStatus;

public record PurchaseQuery(
        UUID companyId,
        PurchaseStatus status,
        UUID supplierId,
        LocalDate from,
        LocalDate to) {
}
