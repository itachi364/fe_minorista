package com.msvanegasg.facturaelectronica.billing.application.port.in;

import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicPosDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.IssueElectronicPosCommand;

public interface IssueElectronicPosUseCase {

    ElectronicPosDocumentResult issue(IssueElectronicPosCommand command);
}
