package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Product;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.ProductJpaEntity;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository.ProductJpaRepository;

@Component
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private static final String DEFAULT_TAX_CATEGORY_CODE = "IVA";
    private static final String DEFAULT_TAX_CODE = "IVA_19";
    private static final String DEFAULT_TAX_LABEL = "IVA 19%";
    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("19");

    private final ProductJpaRepository repository;

    public ProductPersistenceAdapter(ProductJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Product> findByCompanyIdAndId(UUID companyId, UUID id) {
        return repository.findByCompanyIdAndId(companyId, id).map(ProductPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Product> findActiveByCompanyIdAndBarcode(UUID companyId, String barcode) {
        return repository.findByCompanyIdAndBarcodeAndActiveTrue(companyId, barcode)
                .map(ProductPersistenceAdapter::toDomain);
    }

    @Override
    public List<Product> findByCompanyId(UUID companyId, Boolean active) {
        if (active == null) {
            return repository.findByCompanyIdOrderByNameAsc(companyId).stream()
                    .map(ProductPersistenceAdapter::toDomain)
                    .toList();
        }
        return repository.findByCompanyIdAndActiveOrderByNameAsc(companyId, active).stream()
                .map(ProductPersistenceAdapter::toDomain)
                .toList();
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
                defaultText(entity.getTaxCategoryCode(), DEFAULT_TAX_CATEGORY_CODE),
                defaultText(entity.getTaxCode(), DEFAULT_TAX_CODE),
                defaultText(entity.getTaxLabel(), DEFAULT_TAX_LABEL),
                entity.getTaxRate() == null ? DEFAULT_TAX_RATE : entity.getTaxRate(),
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
        entity.setTaxCategoryCode(product.taxCategoryCode());
        entity.setTaxCode(product.taxCode());
        entity.setTaxLabel(product.taxLabel());
        entity.setTaxRate(product.taxRate());
        entity.setActive(product.active());
        entity.setCreatedAt(product.createdAt());
        entity.setUpdatedAt(product.updatedAt());
        return entity;
    }

    private static String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
