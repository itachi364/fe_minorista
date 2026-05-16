package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.billing.application.dto.CalculateElectronicDocumentCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.CalculatedElectronicDocumentLineResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.CalculatedElectronicDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentLineCalculationCommand;
import com.msvanegasg.facturaelectronica.billing.application.port.in.CalculateElectronicDocumentUseCase;
import com.msvanegasg.facturaelectronica.billing.domain.model.CalculatedDocumentLine;
import com.msvanegasg.facturaelectronica.billing.domain.model.CalculatedElectronicDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.DocumentLineToCalculate;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentCalculator;

public class CalculateElectronicDocumentService implements CalculateElectronicDocumentUseCase {

    @Override
    public CalculatedElectronicDocumentResult calculate(CalculateElectronicDocumentCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.documentType(), "documentType is required");
        Objects.requireNonNull(command.lines(), "lines are required");

        CalculatedElectronicDocument calculatedDocument = ElectronicDocumentCalculator.calculate(
                command.lines().stream()
                        .map(CalculateElectronicDocumentService::toDomain)
                        .toList());

        return new CalculatedElectronicDocumentResult(
                calculatedDocument.lines().stream()
                        .map(CalculateElectronicDocumentService::toResult)
                        .toList(),
                calculatedDocument.grossAmount(),
                calculatedDocument.discountTotal(),
                calculatedDocument.subtotal(),
                calculatedDocument.taxTotal(),
                calculatedDocument.total());
    }

    private static DocumentLineToCalculate toDomain(ElectronicDocumentLineCalculationCommand command) {
        return new DocumentLineToCalculate(
                command.productId(),
                command.quantity(),
                command.unitPrice(),
                command.discountAmount(),
                command.taxCode(),
                command.taxRate());
    }

    private static CalculatedElectronicDocumentLineResult toResult(CalculatedDocumentLine line) {
        return new CalculatedElectronicDocumentLineResult(
                line.productId(),
                line.quantity(),
                line.unitPrice(),
                line.discountAmount(),
                line.taxCode(),
                line.taxRate(),
                line.grossAmount(),
                line.taxableAmount(),
                line.taxAmount(),
                line.lineTotal());
    }
}
