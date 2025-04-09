package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.DTO.CategoriaDTO;
import com.msvanegasg.facturaelectronica.models.Categoria;
import com.msvanegasg.facturaelectronica.service.CategoriaService;
import com.msvanegasg.facturaelectronica.mapper.CategoriaMapper;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public List<Categoria> findAll() {
        return categoriaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> findById(@PathVariable("id") Long id) {
        Categoria categoria = categoriaService.findById(id);
        return ResponseEntity.ok(categoria);
    }

    @GetMapping("/nombre/{nombre}")
    public List<Categoria> findByNombre(@PathVariable("nombre") String nombre) {
        return categoriaService.findByNombreContainingIgnoreCase(nombre);
    }

    @PostMapping
    public ResponseEntity<Categoria> save(@Valid @RequestBody CategoriaDTO categoriaDTO) {
        Categoria categoria = CategoriaMapper.toEntity(categoriaDTO);
        Categoria saved = categoriaService.save(categoria);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> update(@PathVariable("id") Long id, @Valid @RequestBody CategoriaDTO categoriaDTO) {
        Categoria existing = categoriaService.findById(id);
        CategoriaMapper.updateEntity(existing, categoriaDTO);
        Categoria updated = categoriaService.save(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disable(@PathVariable("id") Long id) {
        categoriaService.disableById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/activar/{id}")
    public ResponseEntity<Void> enable(@PathVariable("id") Long id) {
        categoriaService.enableById(id);
        return ResponseEntity.noContent().build();
    }
}
