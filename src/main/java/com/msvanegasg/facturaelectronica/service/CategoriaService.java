package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.exception.CategoriaNotFoundException;
import com.msvanegasg.facturaelectronica.models.Categoria;
import com.msvanegasg.facturaelectronica.repository.CategoriaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }

    public Categoria findById(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));
    }

    public Categoria save(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public Categoria update(Long id, Categoria updatedCategoria) {
        Categoria existing = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));

        existing.setNombre(updatedCategoria.getNombre());
        existing.setDescripcion(updatedCategoria.getDescripcion());

        return categoriaRepository.save(existing);
    }

    public void disableById(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));
        categoria.setActivo(false);
        categoriaRepository.save(categoria);
    }

    public void enableById(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));
        categoria.setActivo(true);
        categoriaRepository.save(categoria);
    }

    public List<Categoria> findByNombreContainingIgnoreCase(String nombre) {
        return categoriaRepository.findByNombreIgnoreCaseAndAccent(nombre);
    }

    public List<Categoria> findByActivoTrue() {
        return categoriaRepository.findByActivoTrue();
    }

    public List<Categoria> findByActivoFalse() {
        return categoriaRepository.findByActivoFalse();
    }
}