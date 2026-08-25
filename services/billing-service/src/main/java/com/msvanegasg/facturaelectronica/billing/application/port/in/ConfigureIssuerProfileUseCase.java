package com.msvanegasg.facturaelectronica.billing.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.ConfigureIssuerProfileCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.IssuerProfileResult;

public interface ConfigureIssuerProfileUseCase {
    IssuerProfileResult configure(ConfigureIssuerProfileCommand command);

    IssuerProfileResult activate(UUID companyId, UUID issuerId);

    IssuerProfileResult deactivate(UUID companyId, UUID issuerId);
}
