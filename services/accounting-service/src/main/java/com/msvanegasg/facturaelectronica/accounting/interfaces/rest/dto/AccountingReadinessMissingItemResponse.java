package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

public record AccountingReadinessMissingItemResponse(
        String code,
        String module,
        String message,
        String suggestedAction) {
}
