package com.msvanegasg.facturaelectronica.dianprovider.application.dto;

import java.time.Instant;

public record CertificateMetadata(
        String alias,
        String fingerprint,
        Instant expiresAt) {
}
