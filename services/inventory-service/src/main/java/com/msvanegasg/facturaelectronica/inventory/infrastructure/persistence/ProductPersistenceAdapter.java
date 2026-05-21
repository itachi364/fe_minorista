package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Product;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.ProductJpaEntity;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository.ProductJpaRepository;

@Component
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository repository;

    public ProductPersistenceAdapter(ProductJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Product> findByCompanyIdAndId(UUID companyId, UUID id) {
        return repository.findByCompanyIdAndId(companyId, id).map(ProductPersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsByCompanyIdAndSku(UUID companyId, String sku) {
        return repository.existsByCompanyIdAndSku(companyId, sku);
    }

    @Override
    public Product save(Product product) {
        return toDomain(repository.save(toEntity(product)));
    }

    static Product toDomain(ProductJpaEntity entity) {
        return new Product(entity.getId(), entity.getCompanyId(), entity.getSku(), entity.getBarcode(),
                entity.getName(), entity.getDescription(), entity.getItemType(), entity.isSaleEnabled(),
                entity.isPurchaseEnabled(), entity.isStockTracked(), entity.getSalePrice(), entity.getCost(),
                entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    static ProductJpaEntity toEntity(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId(product.id());
        entity.setCompanyId(product.companyId());
        entity.setSku(product.sku());
        entity.setBarcode(product.barcode());
        entity.setName(product.name());
        entity.setDescription(product.description());
        entity.setItemType(product.itemType());
        entity.setSaleEnabled(product.saleEnabled());
        entity.setPurchaseEnabled(product.purchaseEnabled());
        entity.setStockTracked(product.stockTracked());
        entity.setSalePrice(product.salePrice());
        entity.setCost(product.cost());
        entity.setActive(product.active());
        entity.setCreatedAt(product.createdAt());
        entity.setUpdatedAt(product.updatedAt());
        return entity;
    }
}
