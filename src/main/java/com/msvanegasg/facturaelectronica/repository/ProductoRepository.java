package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Producto;
import com.msvanegasg.facturaelectronica.models.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoria(Categoria categoria);

    List<Producto> findByEstadoTrue();

    List<Producto> findByNombreContainingIgnoreCase(String nombre);
}
