package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.catalog.application.port.out.CategoryRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Category;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.CategoryJpaRepository;

@Component
public class CategoryPersistenceAdapter implements CategoryRepositoryPort {

    private final CategoryJpaRepository categoryJpaRepository;

    public CategoryPersistenceAdapter(CategoryJpaRepository categoryJpaRepository) {
        this.categoryJpaRepository = categoryJpaRepository;
    }

    @Override
    public List<Category> findAll() {
        return categoryJpaRepository.findAll().stream()
                .map(CategoryPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryJpaRepository.findById(id)
                .map(CategoryPersistenceAdapter::toDomain);
    }

    @Override
    public List<Category> findByNameContainingIgnoreCaseAndAccent(String name) {
        return categoryJpaRepository.findByNombreIgnoreCaseAndAccent(name).stream()
                .map(CategoryPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Category save(Category category) {
        CategoryJpaEntity saved = categoryJpaRepository.save(toEntity(category));
        return toDomain(saved);
    }

    private static Category toDomain(CategoryJpaEntity entity) {
        return Category.restore(
                entity.getIdCategoria(),
                entity.getNombre(),
                entity.getDescripcion(),
                Boolean.TRUE.equals(entity.getActivo()));
    }

    private static CategoryJpaEntity toEntity(Category category) {
        return CategoryJpaEntity.builder()
                .idCategoria(category.id())
                .nombre(category.name())
                .descripcion(category.description())
                .activo(category.active())
                .build();
    }
}
