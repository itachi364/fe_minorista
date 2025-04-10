package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Producto;
import com.msvanegasg.facturaelectronica.models.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoria(Categoria categoria);

    List<Producto> findByActivoTrue();
    
    List<Producto> findByActivoFalse();

    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    
    Optional<Producto> findByCodigoBarras(Long codigoBarras);
}
