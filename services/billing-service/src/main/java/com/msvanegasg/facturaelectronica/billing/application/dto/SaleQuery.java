package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.PaymentMethodCode;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleStatus;

public record SaleQuery(UUID companyId, SaleStatus status, LocalDate from, LocalDate to, UUID sellerId,
        UUID customerId, UUID productId, PaymentMethodCode paymentMethodCode,
        ElectronicDocumentStatus documentStatus) {

    public SaleQuery(UUID companyId, SaleStatus status, LocalDate from, LocalDate to) {
        this(companyId, status, from, to, null, null, null, null, null);
    }
}
