package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.DetalleGasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleGastoRepository extends JpaRepository<DetalleGasto, Long> {

    List<DetalleGasto> findByGastoIdGasto(Long idGasto);

}
