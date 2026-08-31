package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msvanegasg.facturaelectronica.inventory.domain.model.PaymentCondition;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseLine;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseStatus;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.PurchaseJpaEntity;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.PurchaseLineJpaEntity;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository.PurchaseJpaRepository;

@ExtendWith(MockitoExtension.class)
class PurchasePersistenceAdapterTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PURCHASE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PRODUCT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID LINE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    @Mock
    private PurchaseJpaRepository repository;

    @Test
    void savesAndRestoresPurchaseWithLines() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        PurchasePersistenceAdapter adapter = new PurchasePersistenceAdapter(repository);

        Purchase saved = adapter.save(purchase());

        assertThat(saved.id()).isEqualTo(PURCHASE_ID);
        assertThat(saved.paymentCondition()).isEqualTo(PaymentCondition.CREDIT);
        assertThat(saved.dueDate()).isEqualTo(LocalDate.of(2026, 6, 20));
        assertThat(saved.lines()).hasSize(1);
        assertThat(saved.lines().get(0).productId()).isEqualTo(PRODUCT_ID);
    }

    @Test
    void findsPurchaseByIdempotencyKey() {
        when(repository.findByCompanyIdAndIdempotencyKey(COMPANY_ID, "purchase-1")).thenReturn(Optional.of(entity()));
        PurchasePersistenceAdapter adapter = new PurchasePersistenceAdapter(repository);

        Optional<Purchase> result = adapter.findByCompanyIdAndIdempotencyKey(COMPANY_ID, "purchase-1");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().status()).isEqualTo(PurchaseStatus.PENDING);
        assertThat(result.orElseThrow().lines()).hasSize(1);
    }

    @Test
    void findsPurchasesWithDynamicFilters() {
        when(repository.findPurchasesDynamic(any(), any(), any(), any(), any())).thenReturn(List.of(entity()));
        PurchasePersistenceAdapter adapter = new PurchasePersistenceAdapter(repository);

        List<Purchase> result = adapter.find(new com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseQuery(
                COMPANY_ID, PurchaseStatus.PENDING, null, LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(PURCHASE_ID);
    }

    private static Purchase purchase() {
        return Purchase.pending(PURCHASE_ID, COMPANY_ID, null, new BigDecimal("90000.00"),
                new BigDecimal("17100.00"), new BigDecimal("107100.00"), PaymentCondition.CREDIT,
                LocalDate.of(2026, 6, 20), null, "purchase-1", NOW,
                List.of(new PurchaseLine(LINE_ID, PURCHASE_ID, PRODUCT_ID, new BigDecimal("10.00"),
                        new BigDecimal("9000.00"), new BigDecimal("90000.00"), new BigDecimal("17100.00"),
                        new BigDecimal("107100.00"))));
    }

    private static PurchaseJpaEntity entity() {
        PurchaseJpaEntity entity = new PurchaseJpaEntity();
        entity.setId(PURCHASE_ID);
        entity.setCompanyId(COMPANY_ID);
        entity.setStatus(PurchaseStatus.PENDING);
        entity.setSubtotal(new BigDecimal("90000.00"));
        entity.setTaxTotal(new BigDecimal("17100.00"));
        entity.setTotal(new BigDecimal("107100.00"));
        entity.setPaymentCondition(PaymentCondition.CREDIT);
        entity.setDueDate(LocalDate.of(2026, 6, 20));
        entity.setIdempotencyKey("purchase-1");
        entity.setCreatedAt(NOW);
        PurchaseLineJpaEntity line = new PurchaseLineJpaEntity();
        line.setId(LINE_ID);
        line.setProductId(PRODUCT_ID);
        line.setQuantity(new BigDecimal("10.00"));
        line.setUnitCost(new BigDecimal("9000.00"));
        line.setSubtotal(new BigDecimal("90000.00"));
        line.setTax(new BigDecimal("17100.00"));
        line.setTotal(new BigDecimal("107100.00"));
        entity.replaceLines(List.of(line));
        return entity;
    }
}
