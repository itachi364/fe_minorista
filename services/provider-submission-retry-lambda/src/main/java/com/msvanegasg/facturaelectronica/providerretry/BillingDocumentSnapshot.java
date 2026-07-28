package com.msvanegasg.facturaelectronica.providerretry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BillingDocumentSnapshot(UUID companyId, UUID saleId, UUID customerId, String saleChannel,
        String saleStatus, UUID documentId, String documentType, ElectronicDocumentStatus documentStatus,
        ProviderStatus providerStatus, String prefix, long documentNumber, String cufeCude, String qrContent,
        BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total, String idempotencyKey, Instant issuedAt,
        List<SaleLineSnapshot> lines) {
}