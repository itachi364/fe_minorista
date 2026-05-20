package com.msvanegasg.facturaelectronica.catalog.interfaces.rest;

import com.msvanegasg.facturaelectronica.catalog.application.dto.CategoryCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Category;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.CategoryRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.CategoryResponse;

public final class CategoryRestMapper {

    private CategoryRestMapper() {
    }

    public static CategoryCommand toCommand(CategoryRequest dto) {
        return new CategoryCommand(dto.getNombre(), dto.getDescripcion());
    }

    public static CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .idCategoria(category.id())
                .nombre(category.name())
                .descripcion(category.description())
                .activo(category.active())
                .build();
    }
}
