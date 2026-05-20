package com.msvanegasg.facturaelectronica.catalog.application.port.in;

import java.util.List;

import com.msvanegasg.facturaelectronica.catalog.application.dto.CategoryCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Category;

public interface ManageCategoryUseCase {

    List<Category> findAll();

    Category findById(Long id);

    List<Category> findByName(String name);

    Category create(CategoryCommand command);

    Category update(Long id, CategoryCommand command);

    void disable(Long id);

    void enable(Long id);
}
