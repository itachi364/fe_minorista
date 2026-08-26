package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.UUID;

public interface OperationalPinValidationPort {

    OperationalPinValidationResult verify(UUID companyId, String pin, String authorizationHeader);

    static OperationalPinValidationPort allowAll() {
        return (companyId, pin, authorizationHeader) -> new OperationalPinValidationResult(true, false, false, 3);
    }

    record OperationalPinValidationResult(boolean valid, boolean locked, boolean mustChange, int remainingAttempts) {
    }
}
