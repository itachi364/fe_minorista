package com.msvanegasg.facturaelectronica.dianprovider.application.dto;

public record DianSignedDocument(String xml, String signatureDigest) {
}
