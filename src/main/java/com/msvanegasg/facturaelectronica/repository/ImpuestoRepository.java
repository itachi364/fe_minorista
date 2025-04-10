package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Impuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImpuestoRepository extends JpaRepository<Impuesto, Long> {

	Impuesto findByActivoTrue();
    
    Impuesto findByActivoFalse();

    List<Impuesto> findByNombreContainingIgnoreCase(String nombre);
    
    Optional<Impuesto> findByTipo(String tipo);
    
    Optional<Impuesto> findByPorcentaje(BigDecimal porcentaje);
}
