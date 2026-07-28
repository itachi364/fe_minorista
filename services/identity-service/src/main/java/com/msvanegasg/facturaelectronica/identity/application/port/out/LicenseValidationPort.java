package com.msvanegasg.facturaelectronica.identity.application.port.out;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.LicenseAction;

public interface LicenseValidationPort {

    void ensureAllowed(UUID companyId, LicenseAction action);
}
