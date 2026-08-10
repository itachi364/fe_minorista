package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.PaymentMethodCode;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.VirtualWalletCode;

public record CreateSaleCommand(UUID companyId, UUID customerId, PaymentMethodCode paymentMethodCode,
        VirtualWalletCode virtualWalletCode, SaleChannel saleChannel, String idempotencyKey, UUID createdBy,
        List<SaleLineCommand> lines) {
}
