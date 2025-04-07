package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    List<Factura> findByClienteIdCliente(Long idCliente);

    List<Factura> findByMetodoPagoIdMetodoPago(Long idMetodoPago);

    List<Factura> findByActivo(Boolean activo);

    List<Factura> findByEstado(String estado);

}