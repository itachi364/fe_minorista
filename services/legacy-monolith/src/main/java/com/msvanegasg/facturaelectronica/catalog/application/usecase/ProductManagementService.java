package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import java.util.List;
import java.util.Objects;

import com.msvanegasg.facturaelectronica.catalog.application.dto.IncreaseProductStockCommand;
import com.msvanegasg.facturaelectronica.catalog.application.dto.ProductCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageProductUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.CategoryRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.ProductRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Category;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Product;
import com.msvanegasg.facturaelectronica.exception.CategoriaNotFoundException;
import com.msvanegasg.facturaelectronica.exception.producto.ProductoCodigoNotFoundException;
import com.msvanegasg.facturaelectronica.exception.producto.ProductoIdNotFoundException;

public class ProductManagementService implements ManageProductUseCase {

    private final ProductRepositoryPort productRepository;
    private final CategoryRepositoryPort categoryRepository;

    public ProductManagementService(ProductRepositoryPort productRepository, CategoryRepositoryPort categoryRepository) {
        this.productRepository = Objects.requireNonNull(productRepository);
        this.categoryRepository = Objects.requireNonNull(categoryRepository);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> findActive() {
        return productRepository.findActive();
    }

    @Override
    public List<Product> findInactive() {
        return productRepository.findInactive();
    }

    @Override
    public List<Product> findByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductoIdNotFoundException(id));
    }

    @Override
    public Product findByBarcode(Long barcode) {
        return productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ProductoCodigoNotFoundException(barcode));
    }

    @Override
    public Product create(ProductCommand command) {
        Objects.requireNonNull(command, "command is required");
        Category category = findCategory(command.categoryId());
        return productRepository.save(Product.create(
                command.name(),
                command.description(),
                command.basePrice(),
                command.stockQuantity(),
                category,
                command.barcode()));
    }

    @Override
    public Product update(Long id, ProductCommand command) {
        Objects.requireNonNull(command, "command is required");
        Product existing = findById(id);
        Category category = findCategory(command.categoryId());
        return productRepository.save(existing.update(
                command.name(),
                command.description(),
                command.basePrice(),
                command.stockQuantity(),
                category,
                command.barcode()));
    }

    @Override
    public void disable(Long id) {
        Product existing = findById(id);
        productRepository.save(existing.disable());
    }

    @Override
    public void enable(Long id) {
        Product existing = findById(id);
        productRepository.save(existing.enable());
    }

    @Override
    public Product increaseStock(IncreaseProductStockCommand command) {
        Objects.requireNonNull(command, "command is required");
        Product product = findByBarcode(command.barcode());
        return productRepository.save(product.increaseStock(command.quantityToAdd()));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));
    }
}
