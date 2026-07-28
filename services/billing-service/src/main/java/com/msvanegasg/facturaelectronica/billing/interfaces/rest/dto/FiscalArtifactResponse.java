package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

public record FiscalArtifactResponse(String type, String storageUri, String contentHash, String content) {
}