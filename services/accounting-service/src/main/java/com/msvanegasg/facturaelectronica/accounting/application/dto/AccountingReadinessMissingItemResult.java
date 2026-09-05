package com.msvanegasg.facturaelectronica.accounting.application.dto;

public record AccountingReadinessMissingItemResult(
        String code,
        String module,
        String message,
        String suggestedAction) {
}
