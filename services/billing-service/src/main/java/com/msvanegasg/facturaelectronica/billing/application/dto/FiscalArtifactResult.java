package com.msvanegasg.facturaelectronica.billing.application.dto;

public record FiscalArtifactResult(String type, String storageUri, String contentHash, String content) {
}