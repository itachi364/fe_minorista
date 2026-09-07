package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreateNumberingResolutionCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.NumberingResolutionResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.CreateNumberingResolutionUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.DianConfigurationReadinessPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.NumberingResolutionRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.NumberingResolution;

public class CreateNumberingResolutionService implements CreateNumberingResolutionUseCase {

    private final NumberingResolutionRepositoryPort numberingResolutionRepository;
    private final IdGeneratorPort idGenerator;
    private final DianConfigurationReadinessPort dianConfigurationReadiness;

    public CreateNumberingResolutionService(NumberingResolutionRepositoryPort numberingResolutionRepository,
            IdGeneratorPort idGenerator) {
        this(numberingResolutionRepository, idGenerator, DianConfigurationReadinessPort.alwaysReady());
    }

    public CreateNumberingResolutionService(NumberingResolutionRepositoryPort numberingResolutionRepository,
            IdGeneratorPort idGenerator, DianConfigurationReadinessPort dianConfigurationReadiness) {
        this.numberingResolutionRepository = Objects.requireNonNull(numberingResolutionRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.dianConfigurationReadiness = Objects.requireNonNull(dianConfigurationReadiness);
    }

    @Override
    public NumberingResolutionResult create(CreateNumberingResolutionCommand command) {
        Objects.requireNonNull(command, "command is required");
        ensureNumberingDocumentType(command.documentType());
        ensureElectronicIssuingReady(command.companyId());
        NumberingResolution saved = numberingResolutionRepository.saveAsOnlyActive(NumberingResolution.create(idGenerator.newId(),
                command.companyId(), command.documentType(), command.resolutionNumber(), command.prefix(),
                command.fromNumber(), command.toNumber(), command.validFrom(), command.validTo(),
                command.environment()));
        return BillingResultMapper.toNumberingResolutionResult(saved);
    }

    @Override
    public NumberingResolutionResult activate(UUID companyId, UUID resolutionId) {
        NumberingResolution resolution = numberingResolutionRepository.findByCompanyIdAndId(companyId, resolutionId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la resolucion de numeracion indicada."));
        ensureNumberingDocumentType(resolution.documentType());
        ensureElectronicIssuingReady(companyId);
        NumberingResolution saved = numberingResolutionRepository.saveAsOnlyActive(resolution.activate());
        return BillingResultMapper.toNumberingResolutionResult(saved, numberingResolutionRepository.usageCount(saved));
    }

    @Override
    public NumberingResolutionResult deactivate(UUID companyId, UUID resolutionId) {
        NumberingResolution resolution = numberingResolutionRepository.findByCompanyIdAndId(companyId, resolutionId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la resolucion de numeracion indicada."));
        NumberingResolution saved = numberingResolutionRepository.save(resolution.deactivate());
        return BillingResultMapper.toNumberingResolutionResult(saved, numberingResolutionRepository.usageCount(saved));
    }

    @Override
    public void delete(UUID companyId, UUID resolutionId) {
        NumberingResolution resolution = numberingResolutionRepository.findByCompanyIdAndId(companyId, resolutionId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la resolucion de numeracion indicada."));
        long usageCount = numberingResolutionRepository.usageCount(resolution);
        if (usageCount > 0) {
            throw new IllegalStateException(
                    "La resolucion de numeracion ya fue usada en documentos fiscales. Inactivala para conservar trazabilidad.");
        }
        numberingResolutionRepository.delete(resolution);
    }

    private static void ensureNumberingDocumentType(com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType documentType) {
        if (documentType == null || !documentType.requiresDianConfiguration()) {
            throw new IllegalArgumentException("El tipo de resolucion debe corresponder a un documento fiscal electronico.");
        }
    }

    private void ensureElectronicIssuingReady(UUID companyId) {
        if (!dianConfigurationReadiness.isReadyForElectronicIssuing(companyId)) {
            throw new IllegalStateException(
                    "Debes configurar, probar y activar DIAN real para esta empresa antes de crear o activar resoluciones electronicas. Si la empresa no esta obligada a transmitir a DIAN, usa venta interna no fiscal.");
        }
    }
}
