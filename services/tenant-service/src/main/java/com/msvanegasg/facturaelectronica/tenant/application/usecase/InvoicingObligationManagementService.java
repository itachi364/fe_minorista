package com.msvanegasg.facturaelectronica.tenant.application.usecase;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.tenant.application.dto.InvoicingObligationCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.InvoicingObligationResult;
import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageInvoicingObligationUseCase;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyFileAssetRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.InvoicingObligationRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyFileCategory;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyInvoicingObligationSnapshot;
import com.msvanegasg.facturaelectronica.tenant.domain.model.InvoicingObligationEngine;
import com.msvanegasg.facturaelectronica.tenant.domain.model.InvoicingObligationInput;
import com.msvanegasg.facturaelectronica.tenant.domain.model.InvoicingObligationStatus;

public class InvoicingObligationManagementService implements ManageInvoicingObligationUseCase {
    private static final String RULESET = "CO-INVOICE-2026-01";

    private final CompanyRepositoryPort companyRepository;
    private final CompanyFileAssetRepositoryPort fileRepository;
    private final InvoicingObligationRepositoryPort repository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;
    private final InvoicingObligationEngine engine;

    public InvoicingObligationManagementService(CompanyRepositoryPort companyRepository,
            CompanyFileAssetRepositoryPort fileRepository, InvoicingObligationRepositoryPort repository,
            IdGeneratorPort idGenerator, ClockPort clock) {
        this(companyRepository, fileRepository, repository, idGenerator, clock, new InvoicingObligationEngine());
    }

    InvoicingObligationManagementService(CompanyRepositoryPort companyRepository,
            CompanyFileAssetRepositoryPort fileRepository, InvoicingObligationRepositoryPort repository,
            IdGeneratorPort idGenerator, ClockPort clock, InvoicingObligationEngine engine) {
        this.companyRepository = Objects.requireNonNull(companyRepository);
        this.fileRepository = Objects.requireNonNull(fileRepository);
        this.repository = Objects.requireNonNull(repository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
        this.engine = Objects.requireNonNull(engine);
    }

    @Override
    @Transactional
    public InvoicingObligationResult evaluate(UUID companyId, InvoicingObligationCommand command) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(command, "command is required");
        companyRepository.findById(companyId).orElseThrow(() -> new CompanyNotFoundException(companyId));
        validateEvidence(companyId, command.rutAssetId());
        InvoicingObligationInput input = toInput(command);
        LocalDate evaluationDate = LocalDate.ofInstant(clock.now(), ZoneOffset.UTC);
        var uvt = repository.findUvtValue(evaluationDate);
        var decision = engine.evaluate(input, uvt);
        var previousStatus = repository.findCurrent(companyId).map(CompanyInvoicingObligationSnapshot::status)
                .orElse(null);
        var persistedStatus = InvoicingObligationStatus.afterReevaluation(previousStatus, decision.status());
        var snapshot = new CompanyInvoicingObligationSnapshot(idGenerator.nextId(), companyId,
                repository.nextVersion(companyId), true, persistedStatus, decision.decisionCode(),
                decision.reasons(), input, uvt, RULESET, command.evaluatedBy(), clock.now());
        return InvoicingObligationResult.from(repository.saveAsCurrent(snapshot));
    }

    @Override
    public InvoicingObligationResult current(UUID companyId) {
        return repository.findCurrent(companyId).map(InvoicingObligationResult::from)
                .orElseThrow(() -> new InvoicingObligationNotFoundException(companyId));
    }

    @Override
    public List<InvoicingObligationResult> history(UUID companyId) {
        return repository.findHistory(companyId).stream().map(InvoicingObligationResult::from).toList();
    }

    private void validateEvidence(UUID companyId, UUID assetId) {
        if (assetId == null) {
            return;
        }
        var asset = fileRepository.findByCompanyIdAndId(companyId, assetId)
                .orElseThrow(() -> new IllegalArgumentException("El soporte RUT no pertenece a la empresa."));
        if (asset.category() != CompanyFileCategory.RUT_EVIDENCE) {
            throw new IllegalArgumentException("El archivo seleccionado no es un soporte RUT.");
        }
    }

    private static InvoicingObligationInput toInput(InvoicingObligationCommand command) {
        return new InvoicingObligationInput(command.personType(), command.taxRegime(), command.rutGeneratedAt(),
                command.rutResponsibilityCodes(), command.ciiuCodes(), command.economicOperationTypes(),
                command.customsUser(), command.establishmentCount(), command.exploitsIntangibles(),
                command.onlyExcludedOrUntaxedOperations(), command.previousYearGrossActivityIncome(),
                command.currentYearGrossActivityIncome(), command.previousYearTaxedActivityFinancialOperations(),
                command.currentYearTaxedActivityFinancialOperations(), command.largestPreviousYearTaxedContract(),
                command.largestCurrentYearTaxedContract(), command.largestSameCustomerAggregate(),
                command.voluntaryElectronicInvoicer(), command.specialExceptionType(),
                command.specialExceptionScope(), command.rutAssetId());
    }
}
