package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.exception.compra.CompraNoEditableException;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ProductPurchaseInfo;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseLineCommand;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductLookupPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductStockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.PurchaseRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.SupplierLookupPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseLine;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseStatus;

class PurchaseManagementServiceTest {

    @Test
    void createPurchaseProcessesPurchaseAndIncreasesStock() {
        InMemoryPurchaseRepository repository = new InMemoryPurchaseRepository();
        RecordingProductStock stock = new RecordingProductStock();
        PurchaseManagementService service = service(repository, stock);

        Purchase result = service.create(command());

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.supplierId()).isEqualTo(99L);
        assertThat(result.status()).isEqualTo(PurchaseStatus.PROCESSED);
        assertThat(result.lines()).hasSize(1);
        assertThat(result.lines().get(0).productId()).isEqualTo(77L);
        assertThat(stock.increases).containsExactly("123456789:3");
    }

    @Test
    void updatePurchaseRejectsProcessedPurchase() {
        InMemoryPurchaseRepository repository = new InMemoryPurchaseRepository();
        repository.save(purchase(PurchaseStatus.PROCESSED));
        PurchaseManagementService service = service(repository, new RecordingProductStock());

        assertThatThrownBy(() -> service.updateIfPending(1L, command()))
                .isInstanceOf(CompraNoEditableException.class);
    }

    @Test
    void findDetailsValidatesPurchaseExistence() {
        InMemoryPurchaseRepository repository = new InMemoryPurchaseRepository();
        repository.save(purchase(PurchaseStatus.PENDING));
        PurchaseManagementService service = service(repository, new RecordingProductStock());

        List<PurchaseLine> details = service.findDetailsByPurchaseId(1L);

        assertThat(details).hasSize(1);
        assertThat(details.get(0).barcode()).isEqualTo(123456789L);
    }

    private static PurchaseManagementService service(InMemoryPurchaseRepository repository, RecordingProductStock stock) {
        return new PurchaseManagementService(repository, new FakeProductLookup(), stock, new FakeSupplierLookup(),
                new FixedClock());
    }

    private static PurchaseCommand command() {
        return new PurchaseCommand(
                900123456L,
                13L,
                BigDecimal.valueOf(300),
                BigDecimal.valueOf(57),
                BigDecimal.valueOf(357),
                "https://evidencia.local/factura.pdf",
                List.of(new PurchaseLineCommand(
                        123456789L,
                        3,
                        BigDecimal.valueOf(100),
                        BigDecimal.valueOf(300),
                        BigDecimal.valueOf(57),
                        BigDecimal.valueOf(357))));
    }

    private static Purchase purchase(PurchaseStatus status) {
        return Purchase.restore(
                1L,
                99L,
                java.time.LocalDateTime.of(2026, 5, 12, 10, 0),
                BigDecimal.valueOf(300),
                BigDecimal.valueOf(57),
                BigDecimal.valueOf(357),
                "https://evidencia.local/factura.pdf",
                status,
                true,
                List.of(PurchaseLine.restore(5L, 123456789L, 3, BigDecimal.valueOf(100), BigDecimal.valueOf(300),
                        BigDecimal.valueOf(57), BigDecimal.valueOf(357), true)));
    }

    private static final class InMemoryPurchaseRepository implements PurchaseRepositoryPort {

        private long nextId = 1L;
        private final Map<Long, Purchase> purchases = new LinkedHashMap<>();

        @Override
        public Purchase save(Purchase purchase) {
            Purchase toSave = purchase.id() == null
                    ? Purchase.restore(nextId++, purchase.supplierId(), purchase.date(), purchase.subtotal(),
                            purchase.taxTotal(), purchase.total(), purchase.evidenceUrl(), purchase.status(),
                            purchase.active(), purchase.lines())
                    : purchase;
            purchases.put(toSave.id(), toSave);
            return toSave;
        }

        @Override
        public Optional<Purchase> findById(Long purchaseId) {
            return Optional.ofNullable(purchases.get(purchaseId));
        }

        @Override
        public List<Purchase> findActive() {
            return purchases.values().stream().filter(Purchase::active).toList();
        }

        @Override
        public boolean existsById(Long purchaseId) {
            return purchases.containsKey(purchaseId);
        }
    }

    private static final class FakeProductLookup implements ProductLookupPort {

        @Override
        public ProductPurchaseInfo findByBarcode(Long barcode) {
            return new ProductPurchaseInfo(77L, barcode);
        }
    }

    private static final class RecordingProductStock implements ProductStockPort {

        private final List<String> increases = new ArrayList<>();

        @Override
        public void increaseStock(Long barcode, Integer quantity) {
            increases.add(barcode + ":" + quantity);
        }
    }

    private static final class FakeSupplierLookup implements SupplierLookupPort {

        @Override
        public Long findSupplierIdByDocument(Long documentNumber, Long documentTypeId) {
            return 99L;
        }
    }

    private static final class FixedClock implements ClockPort {

        @Override
        public Instant now() {
            return Instant.parse("2026-05-12T15:00:00Z");
        }
    }
}
