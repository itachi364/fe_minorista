package com.msvanegasg.facturaelectronica.catalog.application.port.out;

import java.util.List;
import java.util.Optional;

import com.msvanegasg.facturaelectronica.catalog.domain.model.Category;

public interface CategoryRepositoryPort {

    List<Category> findAll();

    Optional<Category> findById(Long id);

    List<Category> findByNameContainingIgnoreCaseAndAccent(String name);

    Category save(Category category);
}
