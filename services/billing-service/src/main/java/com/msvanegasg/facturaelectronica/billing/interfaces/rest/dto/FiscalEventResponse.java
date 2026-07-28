package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record FiscalEventResponse(UUID documentId, String eventType, String result, String detail, Instant occurredAt) {
}