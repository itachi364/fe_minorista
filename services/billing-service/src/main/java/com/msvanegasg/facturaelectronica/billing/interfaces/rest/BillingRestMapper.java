package com.msvanegasg.facturaelectronica.billing.interfaces.rest;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreateSaleCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleLineCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleLineResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleResult;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.ElectronicDocumentResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.SaleLineRequest;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.SaleLineResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.SaleRequest;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.SaleResponse;

final class BillingRestMapper {

    private BillingRestMapper() {
    }

    static CreateSaleCommand toCommand(UUID companyId, SaleRequest request, UUID createdBy, String idempotencyKey) {
        return new CreateSaleCommand(companyId, request.customerId(), request.paymentMethodId(), request.saleChannel(),
                idempotencyKey, createdBy, request.items().stream().map(BillingRestMapper::toLineCommand).toList());
    }

    static SaleResponse toResponse(SaleResult result) {
        return new SaleResponse(result.id(), result.companyId(), result.customerId(), result.paymentMethodId(),
                result.saleChannel(), result.status(), result.subtotal(), result.discountTotal(), result.taxTotal(),
                result.total(), result.idempotencyKey(), result.createdBy(), result.createdAt(), result.confirmedAt(),
                result.lines().stream().map(BillingRestMapper::toLineResponse).toList(),
                result.electronicDocument() == null ? null : toDocumentResponse(result.electronicDocument()));
    }

    private static SaleLineCommand toLineCommand(SaleLineRequest request) {
        return new SaleLineCommand(request.productId(), request.quantity(), request.unitPrice(),
                request.discountAmount(), request.taxCode(), request.taxRate());
    }

    private static SaleLineResponse toLineResponse(SaleLineResult result) {
        return new SaleLineResponse(result.id(), result.productId(), result.quantity(), result.unitPrice(),
                result.discountAmount(), result.taxCode(), result.taxRate(), result.subtotal(), result.taxAmount(),
                result.total());
    }

    private static ElectronicDocumentResponse toDocumentResponse(ElectronicDocumentResult result) {
        return new ElectronicDocumentResponse(result.id(), result.companyId(), result.saleId(), result.documentType(),
                result.status(), result.providerStatus(), result.prefix(), result.documentNumber(), result.cufeCude(),
                result.qrContent(), result.subtotal(), result.taxTotal(), result.total(), result.providerTrackingId(),
                result.providerErrorCode(), result.providerErrorMessage(), result.issuedAt(),
                result.inventoryAppliedAt(), result.accountingAppliedAt());
    }
}
