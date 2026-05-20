package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageCategoryUseCase;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.CategoryRestMapper;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.CategoryRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.CategoryResponse;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final ManageCategoryUseCase manageCategoryUseCase;

    public CategoriaController(ManageCategoryUseCase manageCategoryUseCase) {
        this.manageCategoryUseCase = manageCategoryUseCase;
    }

    @GetMapping
    public List<CategoryResponse> findAll() {
        return manageCategoryUseCase.findAll().stream()
                .map(CategoryRestMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable("id") Long id) {
        CategoryResponse categoria = CategoryRestMapper.toResponse(manageCategoryUseCase.findById(id));
        return ResponseEntity.ok(categoria);
    }

    @GetMapping("/nombre/{nombre}")
    public List<CategoryResponse> findByNombre(@PathVariable("nombre") String nombre) {
        return manageCategoryUseCase.findByName(nombre).stream()
                .map(CategoryRestMapper::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> save(@Valid @RequestBody CategoryRequest categoriaDTO) {
        CategoryResponse saved = CategoryRestMapper.toResponse(
                manageCategoryUseCase.create(CategoryRestMapper.toCommand(categoriaDTO)));
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable("id") Long id,
            @Valid @RequestBody CategoryRequest categoriaDTO) {
        CategoryResponse updated = CategoryRestMapper.toResponse(
                manageCategoryUseCase.update(id, CategoryRestMapper.toCommand(categoriaDTO)));
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disable(@PathVariable("id") Long id) {
        manageCategoryUseCase.disable(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/activar/{id}")
    public ResponseEntity<Void> enable(@PathVariable("id") Long id) {
        manageCategoryUseCase.enable(id);
        return ResponseEntity.noContent().build();
    }
}
