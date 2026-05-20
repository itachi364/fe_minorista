package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.util.UUID;

public record FiscalNumberAssignment(
        UUID resolutionId,
        String resolutionNumber,
        String prefix,
        long number) {
}
