package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long> {

    List<Gasto> findByTipoGastoIdTipoGasto(Long idTipoGasto);

    List<Gasto> findByMetodoPagoIdMetodoPago(Long idMetodoPago);

    List<Gasto> findByActivo(Boolean activo);

    List<Gasto> findByEstado(String estado);
}
