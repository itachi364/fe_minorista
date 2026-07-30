package com.msvanegasg.facturaelectronica.bff.observability;

import java.util.UUID;

public final class CorrelationId {

    public static final String HEADER_NAME = "X-Correlation-Id";
    public static final String REQUEST_ATTRIBUTE = "correlationId";

    private CorrelationId() {
    }

    public static String resolve(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return candidate.strip();
    }
}
