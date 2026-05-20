package com.msvanegasg.facturaelectronica.audit.observability;

import java.util.UUID;

public final class CorrelationId {

    public static final String HEADER_NAME = "X-Correlation-Id";
    public static final String REQUEST_ATTRIBUTE = "correlationId";
    public static final String MDC_KEY = "correlationId";

    private CorrelationId() {
    }

    public static String resolve(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return headerValue.trim();
    }
}
