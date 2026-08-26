package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.billing.application.dto.AssignFiscalNumberCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNumberResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.AssignFiscalNumberUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IssuerProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.NumberingResolutionRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalNumberAssignment;
import com.msvanegasg.facturaelectronica.billing.domain.model.NumberingResolution;

public class AssignFiscalNumberService implements AssignFiscalNumberUseCase {

    private final IssuerProfileRepositoryPort issuerProfileRepository;
    private final NumberingResolutionRepositoryPort numberingResolutionRepository;

    public AssignFiscalNumberService(IssuerProfileRepositoryPort issuerProfileRepository,
            NumberingResolutionRepositoryPort numberingResolutionRepository) {
        this.issuerProfileRepository = Objects.requireNonNull(issuerProfileRepository);
        this.numberingResolutionRepository = Objects.requireNonNull(numberingResolutionRepository);
    }

    @Override
    public FiscalNumberResult assign(AssignFiscalNumberCommand command) {
        Objects.requireNonNull(command, "command is required");
        issuerProfileRepository.findActiveByCompanyId(command.companyId())
                .orElseThrow(() -> new IllegalStateException(
                        "Debes configurar un emisor fiscal activo antes de emitir documentos fiscales."));
        NumberingResolution resolution = numberingResolutionRepository.findActiveResolution(command.companyId(),
                command.documentType(), command.environment(), command.documentDate())
                .orElseThrow(() -> new IllegalStateException(
                        "Debes configurar una resolucion de numeracion activa para "
                                + documentTypeLabel(command.documentType()) + " antes de emitir el documento fiscal."));
        FiscalNumberAssignment assignment = resolution.assignNextNumber(command.companyId(), command.documentType(),
                command.documentDate(), command.environment());
        numberingResolutionRepository.save(resolution);
        return new FiscalNumberResult(assignment.resolutionId(), assignment.resolutionNumber(), assignment.prefix(),
                assignment.number());
    }

    private static String documentTypeLabel(com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType documentType) {
        return switch (documentType) {
            case ELECTRONIC_INVOICE -> "factura electronica de venta";
            case ELECTRONIC_POS -> "POS electronico";
            case CREDIT_NOTE -> "nota credito";
            case DEBIT_NOTE -> "nota debito";
            case POS_ADJUSTMENT_NOTE -> "nota de ajuste POS";
        };
    }
}
