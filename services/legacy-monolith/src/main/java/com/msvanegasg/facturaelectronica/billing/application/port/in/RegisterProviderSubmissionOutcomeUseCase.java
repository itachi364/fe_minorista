package com.msvanegasg.facturaelectronica.billing.application.port.in;

import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentStatusResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.RegisterProviderSubmissionOutcomeCommand;

public interface RegisterProviderSubmissionOutcomeUseCase {

    ElectronicDocumentStatusResult register(RegisterProviderSubmissionOutcomeCommand command);
}
