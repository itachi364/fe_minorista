package com.msvanegasg.facturaelectronica.billing.application.port.in;

import com.msvanegasg.facturaelectronica.billing.application.dto.ConfigureIssuerProfileCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.IssuerProfileResult;

public interface ConfigureIssuerProfileUseCase {

    IssuerProfileResult configure(ConfigureIssuerProfileCommand command);
}
