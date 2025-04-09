package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {

	MetodoPago findByActivoFalse();
	
	MetodoPago findByActivoTrue();

    List<MetodoPago> findByNombreContainingIgnoreCase(String nombre);
}
