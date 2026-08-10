package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.PaymentMethodCode;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.BuyerIdentificationMode;
import com.msvanegasg.facturaelectronica.billing.domain.model.VirtualWalletCode;

public record CreateSaleCommand(UUID companyId, BuyerIdentificationMode buyerIdentificationMode, UUID customerId,
        PaymentMethodCode paymentMethodCode,
        VirtualWalletCode virtualWalletCode, SaleChannel saleChannel, String idempotencyKey, UUID createdBy,
        List<SaleLineCommand> lines) {

    public CreateSaleCommand(UUID companyId, UUID customerId, PaymentMethodCode paymentMethodCode,
            VirtualWalletCode virtualWalletCode, SaleChannel saleChannel, String idempotencyKey, UUID createdBy,
            List<SaleLineCommand> lines) {
        this(companyId, customerId == null ? BuyerIdentificationMode.FINAL_CONSUMER : BuyerIdentificationMode.IDENTIFIED_CUSTOMER,
                customerId, paymentMethodCode, virtualWalletCode, saleChannel, idempotencyKey, createdBy, lines);
    }
}
