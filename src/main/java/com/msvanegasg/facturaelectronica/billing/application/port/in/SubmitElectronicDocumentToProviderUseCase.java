package com.msvanegasg.facturaelectronica.billing.application.port.in;

import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SubmitElectronicDocumentToProviderCommand;

public interface SubmitElectronicDocumentToProviderUseCase {

    ProviderSubmissionResult submit(SubmitElectronicDocumentToProviderCommand command);
}
