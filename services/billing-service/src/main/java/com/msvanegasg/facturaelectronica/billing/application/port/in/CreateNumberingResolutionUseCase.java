package com.msvanegasg.facturaelectronica.billing.application.port.in;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreateNumberingResolutionCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.NumberingResolutionResult;

public interface CreateNumberingResolutionUseCase {
    NumberingResolutionResult create(CreateNumberingResolutionCommand command);
}
