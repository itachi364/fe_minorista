package com.msvanegasg.facturaelectronica.billing.infrastructure.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleLine;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;

class MockElectronicDocumentProviderAdapterTest {

    @Test
    void returnsAcceptedMockResponse() {
        var adapter = new MockElectronicDocumentProviderAdapter(
                new BillingProperties("http://inventory", "http://provider", "http://accounting", "ACCEPTED"));
        UUID documentId = UUID.fromString("55555555-5555-5555-5555-555555555555");

        var result = adapter.submitElectronicPos(sale(), documentId, "confirm-1");

        assertThat(result.status()).isEqualTo(ProviderStatus.ACCEPTED);
        assertThat(result.trackingId()).isEqualTo("mock-" + documentId);
        assertThat(result.cufeCude()).isNotBlank();
        assertThat(result.qrContent()).startsWith("mock-qr:");
    }

    private static Sale sale() {
        UUID companyId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID saleId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID productId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        return Sale.draft(saleId, companyId, null, null, SaleChannel.POS, "sale-1", null,
                Instant.parse("2026-05-19T10:00:00Z"),
                List.of(SaleLine.calculate(UUID.fromString("44444444-4444-4444-4444-444444444444"), productId,
                        new BigDecimal("2.00"), new BigDecimal("15000.00"), BigDecimal.ZERO, "IVA_19",
                        new BigDecimal("19.00"))));
    }
}
