package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import java.util.List;
import java.util.Objects;

import com.msvanegasg.facturaelectronica.catalog.application.dto.CategoryCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageCategoryUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.CategoryRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Category;
import com.msvanegasg.facturaelectronica.exception.CategoriaNotFoundException;

public class CategoryManagementService implements ManageCategoryUseCase {

    private final CategoryRepositoryPort categoryRepository;

    public CategoryManagementService(CategoryRepositoryPort categoryRepository) {
        this.categoryRepository = Objects.requireNonNull(categoryRepository);
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));
    }

    @Override
    public List<Category> findByName(String name) {
        return categoryRepository.findByNameContainingIgnoreCaseAndAccent(name);
    }

    @Override
    public Category create(CategoryCommand command) {
        Objects.requireNonNull(command, "command is required");
        return categoryRepository.save(Category.create(command.name(), command.description()));
    }

    @Override
    public Category update(Long id, CategoryCommand command) {
        Objects.requireNonNull(command, "command is required");
        Category existing = findById(id);
        return categoryRepository.save(existing.update(command.name(), command.description()));
    }

    @Override
    public void disable(Long id) {
        Category existing = findById(id);
        categoryRepository.save(existing.disable());
    }

    @Override
    public void enable(Long id) {
        Category existing = findById(id);
        categoryRepository.save(existing.enable());
    }
}
