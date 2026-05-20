package com.msvanegasg.facturaelectronica.billing.interfaces.rest;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.ConfigureIssuerProfileCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.CreateNumberingResolutionCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentLineCalculationCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicPosDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.IssueElectronicPosCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.IssuerProfileResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.NumberingResolutionResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SubmitElectronicPosDocumentResult;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.ElectronicPosRequest;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.ElectronicPosResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.IssuerProfileRequest;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.IssuerProfileResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.NumberingResolutionRequest;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.NumberingResolutionResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.SubmitElectronicPosResponse;

public final class BillingRestMapper {

    private BillingRestMapper() {
    }

    public static ConfigureIssuerProfileCommand toCommand(UUID companyId, IssuerProfileRequest request) {
        return new ConfigureIssuerProfileCommand(
                companyId,
                request.legalName(),
                request.nit(),
                request.verificationDigit(),
                request.taxResponsibilities(),
                request.municipalityCode(),
                request.address());
    }

    public static IssuerProfileResponse toResponse(IssuerProfileResult result) {
        return new IssuerProfileResponse(
                result.id(),
                result.companyId(),
                result.legalName(),
                result.nit(),
                result.verificationDigit(),
                result.taxResponsibilities(),
                result.municipalityCode(),
                result.address(),
                result.active());
    }

    public static CreateNumberingResolutionCommand toCommand(UUID companyId, NumberingResolutionRequest request) {
        return new CreateNumberingResolutionCommand(
                companyId,
                request.documentType(),
                request.resolutionNumber(),
                request.prefix(),
                request.fromNumber(),
                request.toNumber(),
                request.validFrom(),
                request.validTo(),
                request.environment());
    }

    public static NumberingResolutionResponse toResponse(NumberingResolutionResult result) {
        return new NumberingResolutionResponse(
                result.id(),
                result.companyId(),
                result.documentType(),
                result.resolutionNumber(),
                result.prefix(),
                result.fromNumber(),
                result.toNumber(),
                result.currentNumber(),
                result.validFrom(),
                result.validTo(),
                result.environment(),
                result.active());
    }

    public static IssueElectronicPosCommand toCommand(UUID companyId, ElectronicPosRequest request) {
        return new IssueElectronicPosCommand(
                companyId,
                request.saleId(),
                request.buyerName(),
                request.buyerDocumentType(),
                request.buyerDocumentNumber(),
                request.documentDate(),
                request.environment(),
                request.lines().stream()
                        .map(line -> new ElectronicDocumentLineCalculationCommand(
                                line.productId(),
                                line.quantity(),
                                line.unitPrice(),
                                line.discountAmount(),
                                line.taxCode(),
                                line.taxRate()))
                        .toList());
    }

    public static ElectronicPosResponse toResponse(ElectronicPosDocumentResult result) {
        return new ElectronicPosResponse(
                result.id(),
                result.companyId(),
                result.saleId(),
                result.buyerName(),
                result.buyerDocumentType(),
                result.buyerDocumentNumber(),
                result.prefix(),
                result.number(),
                result.cude(),
                result.subtotal(),
                result.taxTotal(),
                result.total(),
                result.status(),
                result.issueAt());
    }

    public static SubmitElectronicPosResponse toResponse(SubmitElectronicPosDocumentResult result) {
        return new SubmitElectronicPosResponse(
                result.documentId(),
                result.providerSubmissionId(),
                result.providerStatus(),
                result.documentStatus(),
                result.cufeCude(),
                result.qrContent(),
                result.errorCode(),
                result.errorMessage());
    }
}
