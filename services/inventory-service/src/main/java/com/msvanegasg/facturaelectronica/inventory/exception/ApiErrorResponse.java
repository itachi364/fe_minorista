package com.msvanegasg.facturaelectronica.inventory.exception;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(Instant timestamp, int status, ApiErrorCode code, String message, String correlationId,
        List<ApiErrorDetail> details) {
}
