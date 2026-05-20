package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreateNumberingResolutionCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.NumberingResolutionResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.CreateNumberingResolutionUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.NumberingResolutionRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.NumberingResolution;

public class CreateNumberingResolutionService implements CreateNumberingResolutionUseCase {

    private final NumberingResolutionRepositoryPort numberingResolutionRepository;
    private final IdGeneratorPort idGenerator;

    public CreateNumberingResolutionService(NumberingResolutionRepositoryPort numberingResolutionRepository,
            IdGeneratorPort idGenerator) {
        this.numberingResolutionRepository = Objects.requireNonNull(numberingResolutionRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public NumberingResolutionResult create(CreateNumberingResolutionCommand command) {
        Objects.requireNonNull(command, "command is required");
        NumberingResolution saved = numberingResolutionRepository.save(NumberingResolution.create(idGenerator.newId(),
                command.companyId(), command.documentType(), command.resolutionNumber(), command.prefix(),
                command.fromNumber(), command.toNumber(), command.validFrom(), command.validTo(),
                command.environment()));
        return BillingResultMapper.toNumberingResolutionResult(saved);
    }
}
