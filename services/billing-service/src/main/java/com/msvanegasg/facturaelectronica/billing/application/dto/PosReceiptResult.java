package com.msvanegasg.facturaelectronica.billing.application.dto;

public record PosReceiptResult(String filename, String contentType, byte[] content) {
}
