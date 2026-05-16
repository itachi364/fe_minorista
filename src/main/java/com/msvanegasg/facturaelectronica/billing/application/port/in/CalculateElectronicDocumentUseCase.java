package com.msvanegasg.facturaelectronica.billing.application.port.in;

import com.msvanegasg.facturaelectronica.billing.application.dto.CalculateElectronicDocumentCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.CalculatedElectronicDocumentResult;

public interface CalculateElectronicDocumentUseCase {

    CalculatedElectronicDocumentResult calculate(CalculateElectronicDocumentCommand command);
}
