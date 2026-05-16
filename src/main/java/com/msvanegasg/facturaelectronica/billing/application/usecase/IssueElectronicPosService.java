package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.billing.application.dto.AssignFiscalNumberCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentLineCalculationCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicPosDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNumberResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.IssueElectronicPosCommand;
import com.msvanegasg.facturaelectronica.billing.application.port.in.AssignFiscalNumberUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.IssueElectronicPosUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicPosDocumentRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.BuyerInformation;
import com.msvanegasg.facturaelectronica.billing.domain.model.CalculatedElectronicDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.DocumentLineToCalculate;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentCalculator;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicPosDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalNumberAssignment;

public class IssueElectronicPosService implements IssueElectronicPosUseCase {

    private final AssignFiscalNumberUseCase assignFiscalNumberUseCase;
    private final ElectronicPosDocumentRepositoryPort posDocumentRepository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public IssueElectronicPosService(
            AssignFiscalNumberUseCase assignFiscalNumberUseCase,
            ElectronicPosDocumentRepositoryPort posDocumentRepository,
            IdGeneratorPort idGenerator,
            ClockPort clock) {
        this.assignFiscalNumberUseCase = Objects.requireNonNull(assignFiscalNumberUseCase);
        this.posDocumentRepository = Objects.requireNonNull(posDocumentRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public ElectronicPosDocumentResult issue(IssueElectronicPosCommand command) {
        validate(command);

        CalculatedElectronicDocument calculatedDocument = ElectronicDocumentCalculator.calculate(
                command.lines().stream()
                        .map(IssueElectronicPosService::toDomainLine)
                        .toList());

        FiscalNumberResult fiscalNumber = assignFiscalNumberUseCase.assign(new AssignFiscalNumberCommand(
                command.companyId(),
                ElectronicDocumentType.ELECTRONIC_POS,
                command.documentDate(),
                command.environment()));

        BuyerInformation buyerInformation = new BuyerInformation(
                command.buyerName(),
                command.buyerDocumentType(),
                command.buyerDocumentNumber());

        ElectronicPosDocument document = ElectronicPosDocument.issue(
                idGenerator.newId(),
                command.companyId(),
                command.saleId(),
                buyerInformation,
                new FiscalNumberAssignment(
                        fiscalNumber.resolutionId(),
                        fiscalNumber.resolutionNumber(),
                        fiscalNumber.prefix(),
                        fiscalNumber.number()),
                calculatedDocument,
                clock.now());

        ElectronicPosDocument savedDocument = posDocumentRepository.save(document);

        return new ElectronicPosDocumentResult(
                savedDocument.id(),
                savedDocument.companyId(),
                savedDocument.saleId(),
                savedDocument.buyerInformation().name(),
                savedDocument.buyerInformation().documentType(),
                savedDocument.buyerInformation().documentNumber(),
                savedDocument.prefix(),
                savedDocument.number(),
                savedDocument.cude(),
                savedDocument.subtotal(),
                savedDocument.taxTotal(),
                savedDocument.total(),
                savedDocument.status(),
                savedDocument.issueAt());
    }

    private static DocumentLineToCalculate toDomainLine(ElectronicDocumentLineCalculationCommand line) {
        return new DocumentLineToCalculate(
                line.productId(),
                line.quantity(),
                line.unitPrice(),
                line.discountAmount(),
                line.taxCode(),
                line.taxRate());
    }

    private static void validate(IssueElectronicPosCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.documentDate(), "documentDate is required");
        Objects.requireNonNull(command.environment(), "environment is required");
        Objects.requireNonNull(command.lines(), "lines are required");
    }
}
