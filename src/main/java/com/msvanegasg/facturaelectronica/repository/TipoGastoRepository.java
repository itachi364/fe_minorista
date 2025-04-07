package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.TipoGasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoGastoRepository extends JpaRepository<TipoGasto, Long> {

    List<TipoGasto> findByActivoTrue();

    boolean existsByNombreIgnoreCase(String nombre);
}
