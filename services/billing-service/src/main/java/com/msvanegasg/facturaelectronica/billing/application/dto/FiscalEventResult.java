package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.time.Instant;
import java.util.UUID;

public record FiscalEventResult(UUID documentId, String eventType, String result, String detail, Instant occurredAt) {
}