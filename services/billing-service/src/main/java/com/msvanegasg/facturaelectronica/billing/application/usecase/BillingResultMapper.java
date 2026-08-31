package com.msvanegasg.facturaelectronica.billing.application.usecase;

import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNoteResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.IssuerProfileResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.NumberingResolutionResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleLineResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleResult;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalNote;
import com.msvanegasg.facturaelectronica.billing.domain.model.IssuerProfile;
import com.msvanegasg.facturaelectronica.billing.domain.model.NumberingResolution;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleLine;

final class BillingResultMapper {

    private BillingResultMapper() {
    }

    static SaleResult toSaleResult(Sale sale) {
        return new SaleResult(sale.id(), sale.companyId(), sale.buyerIdentificationMode(), sale.customerId(),
                sale.paymentMethodCode(), sale.virtualWalletCode(), sale.saleChannel(), sale.status(), sale.subtotal(),
                sale.discountTotal(), sale.taxTotal(), sale.total(), sale.idempotencyKey(), sale.createdBy(),
                sale.createdAt(), sale.confirmedAt(), sale.lines().stream().map(BillingResultMapper::toLineResult).toList(),
                sale.electronicDocument() == null ? null : toDocumentResult(sale.electronicDocument()));
    }

    private static SaleLineResult toLineResult(SaleLine line) {
        return new SaleLineResult(line.id(), line.productId(), line.productSku(), line.productName(),
                line.itemType(), line.stockTracked(), line.quantity(), line.unitPrice(), line.unitCost(), line.discountAmount(),
                line.taxCode(), line.taxRate(), line.subtotal(), line.taxAmount(), line.total());
    }

    static ElectronicDocumentResult toDocumentResult(ElectronicDocument document) {
        return new ElectronicDocumentResult(document.id(), document.companyId(), document.saleId(),
                document.documentType(), document.status(), document.providerStatus(), document.prefix(),
                document.documentNumber(), document.cufeCude(), document.qrContent(), document.subtotal(),
                document.taxTotal(), document.total(), document.providerTrackingId(), document.providerErrorCode(),
                document.providerErrorMessage(), document.issuedAt(), document.inventoryAppliedAt(),
                document.accountingAppliedAt());
    }

    static FiscalNoteResult toFiscalNoteResult(FiscalNote note) {
        return new FiscalNoteResult(note.id(), note.companyId(), note.originalDocumentId(), note.noteType(),
                note.adjustmentKind(), note.status(), note.providerStatus(), note.reason(), note.prefix(),
                note.documentNumber(), note.cufeCude(), note.qrContent(), note.subtotal(), note.taxTotal(),
                note.total(), note.providerTrackingId(), note.providerErrorCode(), note.providerErrorMessage(),
                note.issuedAt());
    }
    static IssuerProfileResult toIssuerProfileResult(IssuerProfile issuerProfile) {
        return new IssuerProfileResult(issuerProfile.id(), issuerProfile.companyId(), issuerProfile.legalName(),
                issuerProfile.nit(), issuerProfile.verificationDigit(), issuerProfile.taxResponsibilities(),
                issuerProfile.municipalityCode(), issuerProfile.address(), issuerProfile.active());
    }

    static NumberingResolutionResult toNumberingResolutionResult(NumberingResolution resolution) {
        return toNumberingResolutionResult(resolution, 0);
    }

    static NumberingResolutionResult toNumberingResolutionResult(NumberingResolution resolution, long usageCount) {
        return new NumberingResolutionResult(resolution.id(), resolution.companyId(), resolution.documentType(),
                resolution.resolutionNumber(), resolution.prefix(), resolution.fromNumber(), resolution.toNumber(),
                resolution.currentNumber(), resolution.validFrom(), resolution.validTo(), resolution.environment(),
                resolution.active(), usageCount > 0, usageCount);
    }
}
