package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.BuyerIdentificationMode;
import com.msvanegasg.facturaelectronica.billing.domain.model.PaymentMethodCode;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.VirtualWalletCode;

public record SaleResponse(UUID id, UUID companyId, BuyerIdentificationMode buyerIdentificationMode, UUID customerId,
        PaymentMethodCode paymentMethodCode,
        VirtualWalletCode virtualWalletCode, SaleChannel saleChannel, SaleStatus status, BigDecimal subtotal,
        BigDecimal discountTotal, BigDecimal taxTotal, BigDecimal total, String idempotencyKey, UUID createdBy,
        Instant createdAt, Instant confirmedAt, List<SaleLineResponse> lines, ElectronicDocumentResponse electronicDocument) {

    public SaleResponse(UUID id, UUID companyId, UUID customerId, PaymentMethodCode paymentMethodCode,
            VirtualWalletCode virtualWalletCode, SaleChannel saleChannel, SaleStatus status, BigDecimal subtotal,
            BigDecimal discountTotal, BigDecimal taxTotal, BigDecimal total, String idempotencyKey, UUID createdBy,
            Instant createdAt, Instant confirmedAt, List<SaleLineResponse> lines,
            ElectronicDocumentResponse electronicDocument) {
        this(id, companyId, customerId == null ? BuyerIdentificationMode.FINAL_CONSUMER : BuyerIdentificationMode.IDENTIFIED_CUSTOMER,
                customerId, paymentMethodCode, virtualWalletCode, saleChannel, status, subtotal, discountTotal,
                taxTotal, total, idempotencyKey, createdBy, createdAt, confirmedAt, lines, electronicDocument);
    }
}
