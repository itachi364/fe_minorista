package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;

public record CreateSaleCommand(UUID companyId, UUID customerId, UUID paymentMethodId, SaleChannel saleChannel,
        String idempotencyKey, UUID createdBy, List<SaleLineCommand> lines) {
}
