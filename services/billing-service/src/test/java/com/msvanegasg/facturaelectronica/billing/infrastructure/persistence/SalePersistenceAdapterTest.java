package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.billingservice.BillingServiceApplication;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentQuery;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.PaymentMethodCode;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleItemType;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleLine;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = BillingServiceApplication.class)
@Import(SalePersistenceAdapter.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SalePersistenceAdapterTest {

    @Autowired
    private SalePersistenceAdapter adapter;

    @Test
    void loadsSaleAggregateWithLinesOutsideRepositoryTransaction() {
        Sale saved = adapter.save(draftSale());

        Sale found = adapter.findByCompanyIdAndId(saved.companyId(), saved.id()).orElseThrow();

        assertThat(found.id()).isEqualTo(saved.id());
        assertThat(found.lines()).hasSize(1);
        assertThat(found.lines().get(0).productId()).isEqualTo(saved.lines().get(0).productId());
        assertThat(found.lines().get(0).productSku()).isEqualTo("SKU-1");
        assertThat(found.paymentMethodCode()).isEqualTo(PaymentMethodCode.CASH);
        assertThat(found.lines().get(0).itemType()).isEqualTo(SaleItemType.PHYSICAL_GOOD);
        assertThat(found.lines().get(0).stockTracked()).isTrue();
    }

    @Test
    void loadsSaleAggregateWithDocumentOutsideRepositoryTransaction() {
        Sale draft = draftSale();
        Sale confirmed = draft.confirm(validatedDocument(draft), Instant.parse("2026-05-19T10:01:00Z"));
        Sale saved = adapter.save(confirmed);

        Sale found = adapter.findByCompanyIdAndIdempotencyKey(saved.companyId(), saved.idempotencyKey()).orElseThrow();

        assertThat(found.lines()).hasSize(1);
        assertThat(found.electronicDocument()).isNotNull();
        assertThat(found.electronicDocument().providerTrackingId()).startsWith("mock-tracking-");
    }

    @Test
    void findsElectronicDocumentsWithOnlyDateRangeFilters() {
        Sale draft = draftSale();
        Sale confirmed = draft.confirm(validatedDocument(draft), Instant.parse("2026-05-19T10:01:00Z"));
        Sale saved = adapter.save(confirmed);

        List<Sale> documents = adapter.findByElectronicDocument(new ElectronicDocumentQuery(saved.companyId(), null,
                null, null, java.time.LocalDate.parse("2026-05-01"), java.time.LocalDate.parse("2026-05-31"), null,
                null, null));

        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).electronicDocument()).isNotNull();
        assertThat(documents.get(0).electronicDocument().id()).isEqualTo(saved.electronicDocument().id());
    }

    private static Sale draftSale() {
        UUID saleId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        return Sale.draft(saleId, companyId, null, PaymentMethodCode.CASH, null, SaleChannel.POS, "sale-" + saleId, null,
                Instant.parse("2026-05-19T10:00:00Z"),
                List.of(SaleLine.calculate(UUID.randomUUID(), productId, "SKU-1", "Producto",
                        SaleItemType.PHYSICAL_GOOD, true, new BigDecimal("2.00"), new BigDecimal("15000.00"),
                        BigDecimal.ZERO, "IVA_19", new BigDecimal("19.00"))));
    }

    private static ElectronicDocument validatedDocument(Sale sale) {
        UUID documentId = UUID.randomUUID();
        return new ElectronicDocument(documentId, sale.companyId(), sale.id(),
                ElectronicDocumentType.ELECTRONIC_POS, ElectronicDocumentStatus.VALIDATED, ProviderStatus.ACCEPTED,
                "POS", 1, "mock-cude-" + documentId, "mock-qr", new BigDecimal("30000.00"),
                new BigDecimal("5700.00"), new BigDecimal("35700.00"), "mock-tracking-" + documentId, null, null,
                "confirm-" + documentId,
                Instant.parse("2026-05-19T10:01:00Z"), null, null);
    }
}
