package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByNombreContainingIgnoreCase(String nombre);

    List<Categoria> findByActivoTrue();
}
