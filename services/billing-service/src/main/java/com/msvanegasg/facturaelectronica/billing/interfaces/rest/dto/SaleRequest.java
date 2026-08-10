package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.BuyerIdentificationMode;
import com.msvanegasg.facturaelectronica.billing.domain.model.PaymentMethodCode;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.VirtualWalletCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record SaleRequest(BuyerIdentificationMode buyerIdentificationMode, UUID customerId,
        PaymentMethodCode paymentMethodCode, VirtualWalletCode virtualWalletCode, SaleChannel saleChannel,
        @NotEmpty List<@Valid SaleLineRequest> items) {
}
