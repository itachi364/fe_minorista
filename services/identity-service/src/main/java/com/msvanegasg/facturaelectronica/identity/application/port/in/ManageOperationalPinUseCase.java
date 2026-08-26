package com.msvanegasg.facturaelectronica.identity.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.application.dto.OperationalPinCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.OperationalPinResult;

public interface ManageOperationalPinUseCase {

    OperationalPinResult configure(OperationalPinCommand command);

    OperationalPinResult verify(OperationalPinCommand command);

    OperationalPinResult unlock(UUID companyId, String authorizationHeader);

    OperationalPinResult findStatus(UUID companyId, String authorizationHeader);
}
