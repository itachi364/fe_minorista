package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.DetalleCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {

    List<DetalleCompra> findByCompraIdCompra(Long idCompra);

    List<DetalleCompra> findByProductoIdProducto(Long idProducto);

}
