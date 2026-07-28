package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.LicenseAction;

public interface LicenseValidationPort {

    void ensureAllowed(UUID companyId, LicenseAction action);
}
