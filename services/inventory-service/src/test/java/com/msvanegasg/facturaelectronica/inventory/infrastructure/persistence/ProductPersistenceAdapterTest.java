package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryItemType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Product;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.ProductJpaEntity;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository.ProductJpaRepository;

@ExtendWith(MockitoExtension.class)
class ProductPersistenceAdapterTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");

    @Mock
    private ProductJpaRepository repository;

    @Test
    void savesAndRestoresInventoryItemTypeFlags() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ProductPersistenceAdapter adapter = new ProductPersistenceAdapter(repository);

        Product saved = adapter.save(serviceProduct());

        assertThat(saved.itemType()).isEqualTo(InventoryItemType.SERVICE);
        assertThat(saved.saleEnabled()).isTrue();
        assertThat(saved.purchaseEnabled()).isFalse();
        assertThat(saved.stockTracked()).isFalse();
    }


    @Test
    void findsProductsByCompanyAndActiveStatus() {
        when(repository.findByCompanyIdAndActiveOrderByNameAsc(COMPANY_ID, true)).thenReturn(List.of(entity()));
        ProductPersistenceAdapter adapter = new ProductPersistenceAdapter(repository);

        List<Product> result = adapter.findByCompanyId(COMPANY_ID, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(PRODUCT_ID);
    }

    @Test
    void findsProductWithInventoryItemTypeFlags() {
        when(repository.findByCompanyIdAndId(COMPANY_ID, PRODUCT_ID)).thenReturn(Optional.of(entity()));
        ProductPersistenceAdapter adapter = new ProductPersistenceAdapter(repository);

        Optional<Product> result = adapter.findByCompanyIdAndId(COMPANY_ID, PRODUCT_ID);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().itemType()).isEqualTo(InventoryItemType.SUPPLY);
        assertThat(result.orElseThrow().stockTracked()).isTrue();
    }

    private static Product serviceProduct() {
        return Product.create(PRODUCT_ID, COMPANY_ID, "SERV-1", null, "Manicura", null, InventoryItemType.SERVICE,
                true, false, false, new BigDecimal("35000.00"), BigDecimal.ZERO, NOW);
    }

    private static ProductJpaEntity entity() {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId(PRODUCT_ID);
        entity.setCompanyId(COMPANY_ID);
        entity.setSku("SUP-1");
        entity.setName("Esmalte");
        entity.setItemType(InventoryItemType.SUPPLY);
        entity.setSaleEnabled(false);
        entity.setPurchaseEnabled(true);
        entity.setStockTracked(true);
        entity.setSalePrice(BigDecimal.ZERO);
        entity.setCost(new BigDecimal("12000.00"));
        entity.setActive(true);
        entity.setCreatedAt(NOW);
        entity.setUpdatedAt(NOW);
        return entity;
    }
}
