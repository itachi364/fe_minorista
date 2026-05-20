package com.msvanegasg.facturaelectronica.dianprovider.observability;

import java.util.UUID;

public final class CorrelationId {

    public static final String HEADER_NAME = "X-Correlation-Id";
    public static final String REQUEST_ATTRIBUTE = "correlationId";

    private CorrelationId() {
    }

    public static String resolve(String headerValue) {
        return headerValue == null || headerValue.isBlank() ? UUID.randomUUID().toString() : headerValue;
    }
}
