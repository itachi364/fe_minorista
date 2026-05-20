package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.catalog.application.port.out.ProductRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Category;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Product;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.CategoryJpaRepository;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.ProductJpaRepository;
import com.msvanegasg.facturaelectronica.exception.CategoriaNotFoundException;

@Component
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository productRepository;
    private final CategoryJpaRepository categoryRepository;

    public ProductPersistenceAdapter(ProductJpaRepository productRepository, CategoryJpaRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll().stream()
                .map(ProductPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Product> findActive() {
        return productRepository.findByActivoTrue().stream()
                .map(ProductPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Product> findInactive() {
        return productRepository.findByActivoFalse().stream()
                .map(ProductPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByNameContainingIgnoreCase(String name) {
        return productRepository.findByNombreContainingIgnoreCase(name).stream()
                .map(ProductPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id)
                .map(ProductPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Product> findByBarcode(Long barcode) {
        return productRepository.findByCodigoBarras(barcode)
                .map(ProductPersistenceAdapter::toDomain);
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity saved = productRepository.save(toEntity(product));
        return toDomain(saved);
    }

    private ProductJpaEntity toEntity(Product product) {
        CategoryJpaEntity category = categoryRepository.findById(product.category().id())
                .orElseThrow(() -> new CategoriaNotFoundException(product.category().id()));
        return ProductJpaEntity.builder()
                .idProducto(product.id())
                .nombre(product.name())
                .descripcion(product.description())
                .precioBase(product.basePrice())
                .cantidadStock(product.stockQuantity())
                .categoria(category)
                .codigoBarras(product.barcode())
                .activo(product.active())
                .build();
    }

    private static Product toDomain(ProductJpaEntity entity) {
        return Product.restore(
                entity.getIdProducto(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getPrecioBase(),
                entity.getCantidadStock(),
                toCategory(entity.getCategoria()),
                entity.getCodigoBarras(),
                Boolean.TRUE.equals(entity.getActivo()));
    }

    private static Category toCategory(CategoryJpaEntity entity) {
        return Category.restore(
                entity.getIdCategoria(),
                entity.getNombre(),
                entity.getDescripcion(),
                Boolean.TRUE.equals(entity.getActivo()));
    }
}
