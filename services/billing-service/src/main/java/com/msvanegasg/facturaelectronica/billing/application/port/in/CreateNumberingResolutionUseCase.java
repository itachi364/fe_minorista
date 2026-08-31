package com.msvanegasg.facturaelectronica.billing.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreateNumberingResolutionCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.NumberingResolutionResult;

public interface CreateNumberingResolutionUseCase {
    NumberingResolutionResult create(CreateNumberingResolutionCommand command);

    NumberingResolutionResult activate(UUID companyId, UUID resolutionId);

    NumberingResolutionResult deactivate(UUID companyId, UUID resolutionId);

    void delete(UUID companyId, UUID resolutionId);
}
