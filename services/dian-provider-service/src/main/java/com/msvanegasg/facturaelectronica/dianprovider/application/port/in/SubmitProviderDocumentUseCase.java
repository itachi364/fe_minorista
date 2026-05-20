package com.msvanegasg.facturaelectronica.dianprovider.application.port.in;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.SubmitProviderDocumentCommand;

public interface SubmitProviderDocumentUseCase {

    ProviderSubmissionResult submit(SubmitProviderDocumentCommand command);
}
