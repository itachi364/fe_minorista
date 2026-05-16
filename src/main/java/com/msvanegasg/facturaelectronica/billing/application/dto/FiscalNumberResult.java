package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.util.UUID;

public record FiscalNumberResult(
        UUID resolutionId,
        String resolutionNumber,
        String prefix,
        long number) {
}
