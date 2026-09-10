package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WithholdingCertificateResult(UUID id, UUID companyId, UUID thirdPartyId, int year, int version,
        BigDecimal totalBase, BigDecimal totalWithheld, Instant generatedAt) {
}
