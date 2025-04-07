package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.DetalleFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleFacturaRepository extends JpaRepository<DetalleFactura, Long> {

    List<DetalleFactura> findByFacturaIdFactura(Long idFactura);

    List<DetalleFactura> findByProductoIdProducto(Long idProducto);

}
