package com.msvanegasg.facturaelectronica.accounting.exception;

import java.time.Instant;
import java.util.List;

public record AccountingApiErrorResponse(Instant timestamp, int status, AccountingApiErrorCode code, String message,
        String correlationId, List<AccountingApiErrorDetail> details) {
}
