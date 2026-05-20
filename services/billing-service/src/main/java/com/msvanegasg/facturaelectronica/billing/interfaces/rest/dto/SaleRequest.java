package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record SaleRequest(UUID customerId, UUID paymentMethodId, SaleChannel saleChannel,
        @NotEmpty List<@Valid SaleLineRequest> items) {
}
