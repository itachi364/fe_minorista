package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.catalog.application.dto.CategoryCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.CategoryRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Category;
import com.msvanegasg.facturaelectronica.exception.CategoriaNotFoundException;

class CategoryManagementServiceTest {

    @Test
    void createCategoryPersistsActiveCategory() {
        InMemoryCategoryRepository repository = new InMemoryCategoryRepository();
        CategoryManagementService service = new CategoryManagementService(repository);

        Category category = service.create(new CategoryCommand(" Bebidas ", " Liquidos "));

        assertThat(category.id()).isEqualTo(1L);
        assertThat(category.name()).isEqualTo("Bebidas");
        assertThat(category.description()).isEqualTo("Liquidos");
        assertThat(category.active()).isTrue();
    }

    @Test
    void updateCategoryKeepsCurrentActiveState() {
        InMemoryCategoryRepository repository = new InMemoryCategoryRepository();
        Category existing = repository.save(Category.create("Bebidas", "Inicial"));
        repository.save(existing.disable());
        CategoryManagementService service = new CategoryManagementService(repository);

        Category updated = service.update(existing.id(), new CategoryCommand("Snacks", "Secos"));

        assertThat(updated.name()).isEqualTo("Snacks");
        assertThat(updated.description()).isEqualTo("Secos");
        assertThat(updated.active()).isFalse();
    }

    @Test
    void disableCategoryMarksItInactive() {
        InMemoryCategoryRepository repository = new InMemoryCategoryRepository();
        Category category = repository.save(Category.create("Bebidas", "Liquidos"));
        CategoryManagementService service = new CategoryManagementService(repository);

        service.disable(category.id());

        assertThat(repository.findById(category.id())).get().extracting(Category::active).isEqualTo(false);
    }

    @Test
    void enableCategoryMarksItActive() {
        InMemoryCategoryRepository repository = new InMemoryCategoryRepository();
        Category category = repository.save(Category.create("Bebidas", "Liquidos"));
        repository.save(category.disable());
        CategoryManagementService service = new CategoryManagementService(repository);

        service.enable(category.id());

        assertThat(repository.findById(category.id())).get().extracting(Category::active).isEqualTo(true);
    }

    @Test
    void findByIdRejectsMissingCategory() {
        CategoryManagementService service = new CategoryManagementService(new InMemoryCategoryRepository());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(CategoriaNotFoundException.class)
                .hasMessageContaining("99");
    }

    private static final class InMemoryCategoryRepository implements CategoryRepositoryPort {

        private final List<Category> categories = new ArrayList<>();
        private long sequence = 1;

        @Override
        public List<Category> findAll() {
            return List.copyOf(categories);
        }

        @Override
        public Optional<Category> findById(Long id) {
            return categories.stream()
                    .filter(category -> category.id().equals(id))
                    .findFirst();
        }

        @Override
        public List<Category> findByNameContainingIgnoreCaseAndAccent(String name) {
            return categories.stream()
                    .filter(category -> category.name().toLowerCase().contains(name.toLowerCase()))
                    .toList();
        }

        @Override
        public Category save(Category category) {
            Category saved = category.id() == null
                    ? Category.restore(sequence++, category.name(), category.description(), category.active())
                    : category;
            categories.removeIf(current -> current.id().equals(saved.id()));
            categories.add(saved);
            return saved;
        }
    }
}
