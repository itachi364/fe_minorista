package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Cliente;
import com.msvanegasg.facturaelectronica.models.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
	
	Optional<Proveedor> findByNumeroDocumento(Long numeroDocumento);

    List<Proveedor> findByNombreContainingIgnoreCase(String nombre);

    List<Proveedor> findByActivoTrue();

    boolean existsByNumeroDocumento(Long numeroDocumento);
}
