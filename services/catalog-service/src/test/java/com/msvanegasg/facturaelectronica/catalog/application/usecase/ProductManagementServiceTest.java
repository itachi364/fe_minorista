package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.catalog.application.dto.IncreaseProductStockCommand;
import com.msvanegasg.facturaelectronica.catalog.application.dto.ProductCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.CategoryRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.ProductRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Category;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Product;
import com.msvanegasg.facturaelectronica.exception.CategoriaNotFoundException;
import com.msvanegasg.facturaelectronica.exception.producto.ProductoCodigoNotFoundException;

class ProductManagementServiceTest {

    @Test
    void createProductStartsActiveAndUsesExistingCategory() {
        InMemoryProductRepository productRepository = new InMemoryProductRepository();
        ProductManagementService service = new ProductManagementService(
                productRepository,
                new InMemoryCategoryRepository());

        Product product = service.create(command("Cafe", 1L));

        assertThat(product.id()).isEqualTo(1L);
        assertThat(product.name()).isEqualTo("Cafe");
        assertThat(product.basePrice()).isEqualByComparingTo("1500.00");
        assertThat(product.stockQuantity()).isEqualTo(10);
        assertThat(product.category().id()).isEqualTo(1L);
        assertThat(product.barcode()).isEqualTo(7701234567890L);
        assertThat(product.active()).isTrue();
    }

    @Test
    void createProductRejectsMissingCategory() {
        ProductManagementService service = new ProductManagementService(
                new InMemoryProductRepository(),
                new InMemoryCategoryRepository());

        assertThatThrownBy(() -> service.create(command("Cafe", 99L)))
                .isInstanceOf(CategoriaNotFoundException.class);
    }

    @Test
    void updateProductKeepsIdentifierAndActiveState() {
        InMemoryProductRepository productRepository = new InMemoryProductRepository();
        Category category = category();
        productRepository.save(Product.restore(7L, "Cafe", "Inicial", new BigDecimal("1500.00"), 10, category,
                7701234567890L, true));
        ProductManagementService service = new ProductManagementService(productRepository, new InMemoryCategoryRepository());

        Product updated = service.update(7L, command("Cafe premium", 1L));

        assertThat(updated.id()).isEqualTo(7L);
        assertThat(updated.name()).isEqualTo("Cafe premium");
        assertThat(updated.active()).isTrue();
    }

    @Test
    void disableAndEnableProduct() {
        InMemoryProductRepository productRepository = new InMemoryProductRepository();
        productRepository.save(product(7L, true, 10));
        ProductManagementService service = new ProductManagementService(productRepository, new InMemoryCategoryRepository());

        service.disable(7L);
        assertThat(productRepository.findById(7L).orElseThrow().active()).isFalse();

        service.enable(7L);
        assertThat(productRepository.findById(7L).orElseThrow().active()).isTrue();
    }

    @Test
    void increaseStockFindsProductByBarcodeAndPersistsNewQuantity() {
        InMemoryProductRepository productRepository = new InMemoryProductRepository();
        productRepository.save(product(7L, true, 10));
        ProductManagementService service = new ProductManagementService(productRepository, new InMemoryCategoryRepository());

        Product updated = service.increaseStock(new IncreaseProductStockCommand(7701234567890L, 5));

        assertThat(updated.stockQuantity()).isEqualTo(15);
    }

    @Test
    void increaseStockRejectsMissingBarcode() {
        ProductManagementService service = new ProductManagementService(
                new InMemoryProductRepository(),
                new InMemoryCategoryRepository());

        assertThatThrownBy(() -> service.increaseStock(new IncreaseProductStockCommand(999L, 5)))
                .isInstanceOf(ProductoCodigoNotFoundException.class);
    }

    private static ProductCommand command(String name, Long categoryId) {
        return new ProductCommand(name, "Producto de tienda", new BigDecimal("1500.00"), 10, categoryId,
                7701234567890L);
    }

    private static Product product(Long id, boolean active, int stockQuantity) {
        return Product.restore(id, "Cafe", "Producto de tienda", new BigDecimal("1500.00"), stockQuantity, category(),
                7701234567890L, active);
    }

    private static Category category() {
        return Category.restore(1L, "Bebidas", "Categoria de bebidas", true);
    }

    private static final class InMemoryProductRepository implements ProductRepositoryPort {

        private long nextId = 1L;
        private final Map<Long, Product> products = new LinkedHashMap<>();

        @Override
        public List<Product> findAll() {
            return List.copyOf(products.values());
        }

        @Override
        public List<Product> findActive() {
            return products.values().stream().filter(Product::active).toList();
        }

        @Override
        public List<Product> findInactive() {
            return products.values().stream().filter(product -> !product.active()).toList();
        }

        @Override
        public List<Product> findByNameContainingIgnoreCase(String name) {
            return products.values().stream()
                    .filter(product -> product.name().toLowerCase().contains(name.toLowerCase()))
                    .toList();
        }

        @Override
        public Optional<Product> findById(Long id) {
            return Optional.ofNullable(products.get(id));
        }

        @Override
        public Optional<Product> findByBarcode(Long barcode) {
            return products.values().stream()
                    .filter(product -> product.barcode().equals(barcode))
                    .findFirst();
        }

        @Override
        public Product save(Product product) {
            Product toSave = product.id() == null
                    ? Product.restore(nextId++, product.name(), product.description(), product.basePrice(),
                            product.stockQuantity(), product.category(), product.barcode(), product.active())
                    : product;
            products.put(toSave.id(), toSave);
            return toSave;
        }
    }

    private static final class InMemoryCategoryRepository implements CategoryRepositoryPort {

        private final Map<Long, Category> categories = Map.of(1L, category());

        @Override
        public List<Category> findAll() {
            return List.copyOf(categories.values());
        }

        @Override
        public Optional<Category> findById(Long id) {
            return Optional.ofNullable(categories.get(id));
        }

        @Override
        public List<Category> findByNameContainingIgnoreCaseAndAccent(String name) {
            return categories.values().stream()
                    .filter(category -> category.name().toLowerCase().contains(name.toLowerCase()))
                    .toList();
        }

        @Override
        public Category save(Category category) {
            throw new UnsupportedOperationException("not needed by this test");
        }
    }
}
