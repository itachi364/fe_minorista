package com.msvanegasg.facturaelectronica.billing.application.usecase;

import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleLineResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleResult;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleLine;

final class BillingResultMapper {

    private BillingResultMapper() {
    }

    static SaleResult toSaleResult(Sale sale) {
        return new SaleResult(sale.id(), sale.companyId(), sale.customerId(), sale.paymentMethodId(),
                sale.saleChannel(), sale.status(), sale.subtotal(), sale.discountTotal(), sale.taxTotal(),
                sale.total(), sale.idempotencyKey(), sale.createdBy(), sale.createdAt(), sale.confirmedAt(),
                sale.lines().stream().map(BillingResultMapper::toLineResult).toList(),
                sale.electronicDocument() == null ? null : toDocumentResult(sale.electronicDocument()));
    }

    private static SaleLineResult toLineResult(SaleLine line) {
        return new SaleLineResult(line.id(), line.productId(), line.quantity(), line.unitPrice(),
                line.discountAmount(), line.taxCode(), line.taxRate(), line.subtotal(), line.taxAmount(),
                line.total());
    }

    private static ElectronicDocumentResult toDocumentResult(ElectronicDocument document) {
        return new ElectronicDocumentResult(document.id(), document.companyId(), document.saleId(),
                document.documentType(), document.status(), document.providerStatus(), document.prefix(),
                document.documentNumber(), document.cufeCude(), document.qrContent(), document.subtotal(),
                document.taxTotal(), document.total(), document.providerTrackingId(), document.providerErrorCode(),
                document.providerErrorMessage(), document.issuedAt(), document.inventoryAppliedAt(),
                document.accountingAppliedAt());
    }
}
