package com.msvanegasg.facturaelectronica.billing.interfaces.rest;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.ConfigureIssuerProfileCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.CreateNumberingResolutionCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.CreateSaleCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalArtifactResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalEventResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNoteResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.IssuerProfileResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.NumberingResolutionResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleLineCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleLineResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleResult;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.ElectronicDocumentResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.FiscalArtifactResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.FiscalEventResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.FiscalNoteResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.IssuerProfileRequest;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.IssuerProfileResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.NumberingResolutionRequest;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.NumberingResolutionResponse;
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

    static ConfigureIssuerProfileCommand toCommand(UUID companyId, IssuerProfileRequest request) {
        return new ConfigureIssuerProfileCommand(companyId, request.legalName(), request.nit(),
                request.verificationDigit(), request.taxResponsibilities(), request.municipalityCode(),
                request.address());
    }

    static IssuerProfileResponse toResponse(IssuerProfileResult result) {
        return new IssuerProfileResponse(result.id(), result.companyId(), result.legalName(), result.nit(),
                result.verificationDigit(), result.taxResponsibilities(), result.municipalityCode(), result.address(),
                result.active());
    }

    static CreateNumberingResolutionCommand toCommand(UUID companyId, NumberingResolutionRequest request) {
        return new CreateNumberingResolutionCommand(companyId, request.documentType(), request.resolutionNumber(),
                request.prefix(), request.fromNumber(), request.toNumber(), request.validFrom(), request.validTo(),
                request.environment());
    }

    static NumberingResolutionResponse toResponse(NumberingResolutionResult result) {
        return new NumberingResolutionResponse(result.id(), result.companyId(), result.documentType(),
                result.resolutionNumber(), result.prefix(), result.fromNumber(), result.toNumber(),
                result.currentNumber(), result.validFrom(), result.validTo(), result.environment(), result.active());
    }

    private static SaleLineCommand toLineCommand(SaleLineRequest request) {
        return new SaleLineCommand(request.productId(), request.quantity(), request.unitPrice(),
                request.discountAmount(), request.taxCode(), request.taxRate());
    }

    private static SaleLineResponse toLineResponse(SaleLineResult result) {
        return new SaleLineResponse(result.id(), result.productId(), result.productSku(), result.productName(),
                result.itemType(), result.stockTracked(), result.quantity(), result.unitPrice(), result.unitCost(),
                result.discountAmount(), result.taxCode(), result.taxRate(), result.subtotal(), result.taxAmount(),
                result.total());
    }

    static FiscalNoteResponse toResponse(FiscalNoteResult result) {
        return new FiscalNoteResponse(result.id(), result.companyId(), result.originalDocumentId(), result.noteType(),
                result.adjustmentKind(), result.status(), result.providerStatus(), result.reason(), result.prefix(),
                result.documentNumber(), result.cufeCude(), result.qrContent(), result.subtotal(), result.taxTotal(),
                result.total(), result.providerTrackingId(), result.providerErrorCode(), result.providerErrorMessage(),
                result.issuedAt());
    }
    static FiscalArtifactResponse toResponse(FiscalArtifactResult result) {
        return new FiscalArtifactResponse(result.type(), result.storageUri(), result.contentHash(), result.content());
    }

    static FiscalEventResponse toResponse(FiscalEventResult result) {
        return new FiscalEventResponse(result.documentId(), result.eventType(), result.result(), result.detail(),
                result.occurredAt());
    }

    static ElectronicDocumentResponse toDocumentResponse(ElectronicDocumentResult result) {
        return new ElectronicDocumentResponse(result.id(), result.companyId(), result.saleId(), result.documentType(),
                result.status(), result.providerStatus(), result.prefix(), result.documentNumber(), result.cufeCude(),
                result.qrContent(), result.subtotal(), result.taxTotal(), result.total(), result.providerTrackingId(),
                result.providerErrorCode(), result.providerErrorMessage(), result.issuedAt(),
                result.inventoryAppliedAt(), result.accountingAppliedAt());
    }
}
