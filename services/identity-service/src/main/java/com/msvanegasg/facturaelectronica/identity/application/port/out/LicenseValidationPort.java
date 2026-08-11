package com.msvanegasg.facturaelectronica.identity.application.port.out;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.application.dto.LicensePolicy;
import com.msvanegasg.facturaelectronica.identity.domain.model.LicenseAction;

public interface LicenseValidationPort {

    void ensureAllowed(UUID companyId, LicenseAction action);

    default LicensePolicy policy(UUID companyId, LicenseAction action) {
        ensureAllowed(companyId, action);
        return LicensePolicy.unlimited();
    }
}
